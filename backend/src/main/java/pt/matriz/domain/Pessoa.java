package pt.matriz.domain;

/** Referência curta a um utilizador (id e nome), por exemplo o criador ou um responsável de uma tarefa. */
public record Pessoa(long id, String nome) {
}
