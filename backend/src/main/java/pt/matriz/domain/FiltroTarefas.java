package pt.matriz.domain;

/** Filtros da listagem; os que forem {@code null} não se aplicam. Combinam-se com E lógico. */
public record FiltroTarefas(EstadoTarefa estado, Prioridade prioridade, Long responsavelId) {
}
