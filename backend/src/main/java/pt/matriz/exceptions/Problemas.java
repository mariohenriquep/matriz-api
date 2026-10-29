package pt.matriz.exceptions;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/** Construção das respostas de erro no formato do contrato ({@code Problema}). */
public final class Problemas {

    private Problemas() {
    }

    public static ProblemDetail criar(HttpStatus estado, String tipo, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalhe);
        problema.setType(URI.create(tipo));
        problema.setTitle(titulo);
        return problema;
    }

    public static ProblemDetail validacao(String detalhe, List<Map<String, String>> erros) {
        ProblemDetail problema = criar(HttpStatus.BAD_REQUEST, "/erros/validacao", "Pedido inválido", detalhe);
        if (!erros.isEmpty()) {
            problema.setProperty("erros", erros);
        }
        return problema;
    }
}
