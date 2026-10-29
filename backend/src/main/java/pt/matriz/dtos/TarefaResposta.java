package pt.matriz.dtos;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import pt.matriz.domain.EstadoTarefa;
import pt.matriz.domain.Prioridade;

/** Schema {@code Tarefa} do contrato. */
public record TarefaResposta(String id, String projetoId, String titulo, String descricao, EstadoTarefa estado,
        Prioridade prioridade, List<ResumoUtilizador> responsaveis, ResumoUtilizador criador, LocalDate dataLimite,
        boolean atrasada, long totalComentarios, OffsetDateTime criadoEm, OffsetDateTime atualizadoEm) {
}
