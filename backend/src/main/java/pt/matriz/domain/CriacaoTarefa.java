package pt.matriz.domain;

import java.time.LocalDate;
import java.util.List;

/** Dados de uma tarefa nova. {@code prioridade} nula fica {@code MEDIA}; a lista de responsáveis pode estar vazia. */
public record CriacaoTarefa(String titulo, String descricao, Prioridade prioridade, LocalDate dataLimite,
        List<Long> responsavelIds) {
}
