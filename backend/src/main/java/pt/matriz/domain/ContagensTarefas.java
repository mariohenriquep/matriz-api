package pt.matriz.domain;

/** Número de tarefas de um projeto em cada estado (painel inicial, RF-14). */
public record ContagensTarefas(long porFazer, long emCurso, long concluidas) {

    public long total() {
        return porFazer + emCurso + concluidas;
    }
}
