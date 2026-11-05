package pt.matriz.exceptions;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

/** Converte as exceções em respostas Problem Details com mensagens em português (RNF-06). */
@RestControllerAdvice
public class TratamentoErros {

    private static final Logger LOG = LoggerFactory.getLogger(TratamentoErros.class);

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

    /** Rota que não existe (ex.: /api/v1/nada). Sem isto, o Spring respondia num formato diferente do contrato. */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ProblemDetail rotaInexistente(NoResourceFoundException erro) {
        return Problemas.criar(HttpStatus.NOT_FOUND, "/erros/nao-encontrado", "Não encontrado", "Recurso não encontrado");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    ProblemDetail metodoNaoSuportado(HttpRequestMethodNotSupportedException erro) {
        return Problemas.criar(HttpStatus.METHOD_NOT_ALLOWED, "/erros/metodo-nao-suportado", "Método não suportado",
                "O método " + erro.getMethod() + " não é suportado neste endereço");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    ProblemDetail tipoNaoSuportado(HttpMediaTypeNotSupportedException erro) {
        return Problemas.criar(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "/erros/tipo-nao-suportado", "Tipo não suportado",
                "O corpo do pedido tem de ser JSON (Content-Type: application/json)");
    }

    /**
     * Qualquer outro erro. Os do Spring MVC que já trazem um código 4xx (ex.: cabeçalho em falta) mantêm-no;
     * os restantes são 500: ficam no log e a resposta não revela detalhes internos.
     */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> erroInesperado(Exception erro) {
        if (erro instanceof ErrorResponse resposta && resposta.getStatusCode().is4xxClientError()) {
            HttpStatus estado = HttpStatus.valueOf(resposta.getStatusCode().value());
            return ResponseEntity.status(estado).body(Problemas.criar(estado, "/erros/pedido-invalido",
                    "Pedido inválido", "O pedido não pôde ser processado"));
        }
        LOG.error("Erro inesperado", erro);
        return ResponseEntity.internalServerError().body(Problemas.criar(HttpStatus.INTERNAL_SERVER_ERROR,
                "/erros/interno", "Erro interno", "Ocorreu um erro inesperado. Tenta novamente mais tarde."));
    }
}
