package pt.matriz.domain;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/** Uma tarefa de um projeto, com os responsáveis e o número de comentários. */
public record Tarefa(
        long id,
        long projetoId,
        String titulo,
        String descricao,
        EstadoTarefa estado,
        Prioridade prioridade,
        List<Pessoa> responsaveis,
        Pessoa criador,
        LocalDate dataLimite,
        long totalComentarios,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm) {

    /** "Atrasada" não é guardada: data limite já passou e a tarefa não está concluída (P-27). */
    public boolean atrasada(LocalDate hoje) {
        return dataLimite != null && dataLimite.isBefore(hoje) && estado != EstadoTarefa.CONCLUIDA;
    }

    public Tarefa comResponsaveis(List<Pessoa> novos) {
        return new Tarefa(id, projetoId, titulo, descricao, estado, prioridade, List.copyOf(novos), criador,
                dataLimite, totalComentarios, criadoEm, atualizadoEm);
    }
}
