package pt.matriz.dtos;

import java.time.OffsetDateTime;

import pt.matriz.domain.Papel;

/** Schema {@code Projeto} do contrato. */
public record ProjetoResposta(String id, String nome, String descricao, ResumoUtilizador gestor, Papel meuPapel,
        Contagens contagens, OffsetDateTime criadoEm, OffsetDateTime atualizadoEm) {
}
