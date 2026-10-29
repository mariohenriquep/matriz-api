package pt.matriz.domain;

import java.time.LocalDate;

/**
 * Alterações pedidas a uma tarefa. Cada indicador {@code alterarX} diz se o campo foi enviado;
 * {@code descricao} e {@code dataLimite} a {@code null} apagam o valor.
 */
public record EdicaoTarefa(
        boolean alterarTitulo, String titulo,
        boolean alterarDescricao, String descricao,
        boolean alterarEstado, EstadoTarefa estado,
        boolean alterarPrioridade, Prioridade prioridade,
        boolean alterarDataLimite, LocalDate dataLimite) {

    public boolean vazia() {
        return !alterarTitulo && !alterarDescricao && !alterarEstado && !alterarPrioridade && !alterarDataLimite;
    }
}
