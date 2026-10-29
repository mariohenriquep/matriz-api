package pt.matriz.domain;

/** Resultado de um login com sucesso. */
public record SessaoIniciada(TokenEmitido token, Utilizador utilizador) {
}
