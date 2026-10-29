package pt.matriz.domain;

import java.time.OffsetDateTime;

/** Comentário de uma tarefa. O autor e a data são sempre definidos pelo servidor. */
public record Comentario(long id, long tarefaId, String conteudo, Pessoa autor, OffsetDateTime criadoEm) {
}
