package pt.matriz.dtos;

import java.time.OffsetDateTime;

/** Schema {@code Comentario} do contrato. */
public record ComentarioResposta(String id, String conteudo, ResumoUtilizador autor, OffsetDateTime criadoEm) {
}
