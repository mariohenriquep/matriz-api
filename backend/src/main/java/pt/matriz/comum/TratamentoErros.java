package pt.matriz.comum;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Converte as exceções em respostas Problem Details com mensagens em português (RNF-06). */
@RestControllerAdvice
public class TratamentoErros {

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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail corpoIlegivel(HttpMessageNotReadableException erro) {
        return Problemas.validacao("O corpo do pedido não é JSON válido ou tem campos com tipo errado", List.of());
    }
}
