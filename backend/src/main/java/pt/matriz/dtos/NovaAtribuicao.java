package pt.matriz.dtos;

/** Corpo do {@code POST /tarefas/{id}/responsaveis}. O id chega como texto, como no contrato. */
public record NovaAtribuicao(String utilizadorId) {
}
