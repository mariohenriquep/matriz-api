package pt.matriz.dtos;

import pt.matriz.domain.Papel;

/** Schema {@code Membro} do contrato. */
public record MembroResposta(UtilizadorResposta utilizador, Papel papel) {
}
