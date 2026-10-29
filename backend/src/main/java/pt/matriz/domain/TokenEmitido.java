package pt.matriz.domain;

import java.time.Instant;

/** Token de sessão entregue no login, com o instante em que deixa de ser aceite. */
public record TokenEmitido(String token, Instant expiraEm) {
}
