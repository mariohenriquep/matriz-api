package pt.matriz.mappers;

import pt.matriz.domain.Comentario;
import pt.matriz.dtos.ComentarioResposta;
import pt.matriz.dtos.ResumoUtilizador;

/** Converte comentários do domínio no DTO de {@code /tarefas/{id}/comentarios}. */
public final class ComentarioMapper {

    private ComentarioMapper() {
    }

    public static ComentarioResposta paraResposta(Comentario comentario) {
        return new ComentarioResposta(String.valueOf(comentario.id()), comentario.conteudo(),
                new ResumoUtilizador(String.valueOf(comentario.autor().id()), comentario.autor().nome()),
                comentario.criadoEm());
    }
}
