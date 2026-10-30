package pt.matriz.dtos;

/** Corpo do {@code POST /tarefas/{id}/comentarios}: só o conteúdo; autor e data vêm do servidor. */
public record NovoComentario(String conteudo) {
}
