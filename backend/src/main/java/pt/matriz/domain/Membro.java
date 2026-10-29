package pt.matriz.domain;

/** Um utilizador que pertence a um projeto, com o papel que lá tem. */
public record Membro(long id, String nome, String email, Papel papel) {
}
