package pt.matriz.dtos;

import java.time.Instant;

/** Resposta do login: o token, quando expira e quem iniciou sessão. */
public record SessaoResposta(String token, Instant expiraEm, UtilizadorResposta utilizador) {
}
