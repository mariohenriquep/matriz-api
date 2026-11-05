package pt.matriz.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.matriz.domain.Comentario;
import pt.matriz.exceptions.ErroApi;
import pt.matriz.repositories.ComentarioRepositorio;

/** Comentários das tarefas (Ficha 03, Ex. 6). Só membros do projeto; não se editam nem apagam. */
@Service
public class ServicoComentarios {

    static final int MAX_CONTEUDO = 2000;

    /** Guardado sem espaços nas pontas; só espaços conta como vazio. */
    private static final CampoTexto CONTEUDO =
            CampoTexto.obrigatorio("conteudo", "O comentário", MAX_CONTEUDO, "O comentário não pode estar vazio");

    private final ComentarioRepositorio comentarios;
    private final ControloAcesso acesso;

    public ServicoComentarios(ComentarioRepositorio comentarios, ControloAcesso acesso) {
        this.comentarios = comentarios;
        this.acesso = acesso;
    }

    @Transactional(readOnly = true)
    public List<Comentario> listar(long tarefa, long utilizador) {
        acesso.exigirMembroDaTarefa(tarefa, utilizador);
        return comentarios.listar(tarefa);
    }

    /** O autor é o utilizador autenticado e a data é a do servidor. */
    @Transactional
    public Comentario criar(long tarefa, long utilizador, String conteudo) {
        acesso.exigirMembroDaTarefa(tarefa, utilizador);
        long id = comentarios.criar(tarefa, utilizador, CONTEUDO.validar(conteudo));
        return comentarios.obter(id).orElseThrow();
    }
}
