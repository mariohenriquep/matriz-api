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
        long id = comentarios.criar(tarefa, utilizador, conteudoValido(conteudo));
        return comentarios.obter(id).orElseThrow();
    }

    /** Guardado sem espaços nas pontas; só espaços conta como vazio. */
    private static String conteudoValido(String conteudo) {
        if (conteudo == null || conteudo.isBlank()) {
            throw ErroApi.validacao("conteudo", "O comentário não pode estar vazio");
        }
        String limpo = conteudo.trim();
        if (limpo.length() > MAX_CONTEUDO) {
            throw ErroApi.validacao("conteudo", "O comentário tem no máximo " + MAX_CONTEUDO + " caracteres");
        }
        return limpo;
    }
}
