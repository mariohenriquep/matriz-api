package pt.matriz.dtos;

/** Corpo do {@code POST /projetos}. A validação é feita no serviço, para dar as mesmas mensagens que o PATCH. */
public record NovoProjeto(String nome, String descricao) {
}
