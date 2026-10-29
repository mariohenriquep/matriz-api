package pt.matriz.dtos;

import java.time.LocalDate;
import java.util.List;

import pt.matriz.domain.Prioridade;

/** Corpo do {@code POST /projetos/{id}/tarefas}. Os ids dos responsáveis chegam como texto, como no contrato. */
public record NovaTarefa(String titulo, String descricao, Prioridade prioridade, LocalDate dataLimite,
        List<String> responsavelIds) {
}
