package pt.matriz.domain;

import java.time.OffsetDateTime;

/** Um projeto visto por um utilizador: inclui o papel dele e as contagens de tarefas por estado. */
public record Projeto(
        long id,
        String nome,
        String descricao,
        long gestorId,
        String gestorNome,
        Papel meuPapel,
        ContagensTarefas contagens,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm,
        /** {@code null} se o projeto estiver ativo. */
        OffsetDateTime arquivadoEm) {

    public boolean arquivado() {
        return arquivadoEm != null;
    }
}
