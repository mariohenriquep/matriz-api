package pt.matriz.exceptions;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

/** Converte as exceções em respostas Problem Details com mensagens em português (RNF-06). */
@RestControllerAdvice
public class TratamentoErros {

    @ExceptionHandler(ErroValidacao.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail validacaoNoServico(ErroValidacao erro) {
        return Problemas.validacao(erro.getMessage(), List.of(Map.of("campo", erro.campo(), "mensagem", erro.getMessage())));
    }

    @ExceptionHandler(ErroApi.class)
    ProblemDetail erroApi(ErroApi erro) {
        return Problemas.criar(erro.estado(), erro.tipo(), erro.titulo(), erro.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail validacao(MethodArgumentNotValidException erro) {
        List<Map<String, String>> erros = erro.getBindingResult().getFieldErrors().stream()
                .map(campo -> Map.of("campo", campo.getField(), "mensagem", String.valueOf(campo.getDefaultMessage())))
                .toList();
        String detalhe = erros.isEmpty() ? "Pedido inválido" : erros.getFirst().get("mensagem");
        return Problemas.validacao(detalhe, erros);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail parametroInvalido(MethodArgumentTypeMismatchException erro) {
        if (erro.getParameter().hasParameterAnnotation(PathVariable.class)) {
            // Um id de caminho não numérico (ex.: /projetos/abc) não identifica nenhum recurso
            return Problemas.criar(HttpStatus.NOT_FOUND, "/erros/nao-encontrado", "Não encontrado",
                    "Recurso não encontrado");
        }
        return Problemas.validacao("Valor inválido para " + erro.getName(),
                List.of(Map.of("campo", erro.getName(), "mensagem", "Valor inválido")));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail corpoIlegivel(HttpMessageNotReadableException erro) {
        // Valor com tipo errado (ex.: estado inexistente, data mal formada) ou campo desconhecido: indica o campo
        if (erro.getCause() instanceof JsonMappingException mapeamento && !mapeamento.getPath().isEmpty()) {
            String campo = mapeamento.getPath().getFirst().getFieldName();
            if (campo != null) {
                String mensagem = erro.getCause() instanceof UnrecognizedPropertyException
                        ? "Campo não permitido: " + campo
                        : "Valor inválido para " + campo;
                return Problemas.validacao(mensagem, List.of(Map.of("campo", campo, "mensagem", mensagem)));
            }
        }
        return Problemas.validacao("O corpo do pedido não é JSON válido ou tem campos com tipo errado", List.of());
    }
}
