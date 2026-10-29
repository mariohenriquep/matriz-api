package pt.matriz.projetos;

import java.time.OffsetDateTime;

/** Um projeto visto por um utilizador: inclui o papel dele e as contagens de tarefas por estado. */
public record ProjetoVista(
        long id,
        String nome,
        String descricao,
        long gestorId,
        String gestorNome,
        Papel meuPapel,
        long porFazer,
        long emCurso,
        long concluidas,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm) {
}
