package pt.matriz.services;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.matriz.domain.EdicaoProjeto;
import pt.matriz.domain.Membro;
import pt.matriz.domain.Projeto;
import pt.matriz.exceptions.ErroApi;
import pt.matriz.repositories.ProjetoRepositorio;

/** Projetos e membros (Ficha 03, Ex. 4). Cada operação verifica primeiro o acesso com {@link ControloAcesso}. */
@Service
public class ServicoProjetos {

    static final int MAX_NOME = 100;
    static final int MAX_DESCRICAO = 1000;
    static final int MAX_PESQUISA = 100;

    private final ProjetoRepositorio projetos;
    private final ControloAcesso acesso;

    public ServicoProjetos(ProjetoRepositorio projetos, ControloAcesso acesso) {
        this.projetos = projetos;
        this.acesso = acesso;
    }

    @Transactional(readOnly = true)
    public List<Projeto> listar(long utilizador, String pesquisa) {
        if (pesquisa != null && pesquisa.length() > MAX_PESQUISA) {
            throw ErroApi.validacao("pesquisa", "A pesquisa tem no máximo " + MAX_PESQUISA + " caracteres");
        }
        String texto = pesquisa == null || pesquisa.isBlank() ? null : pesquisa.trim();
        return projetos.listar(utilizador, texto);
    }

    @Transactional(readOnly = true)
    public Projeto obter(long projeto, long utilizador) {
        acesso.exigirMembro(projeto, utilizador);
        return projetos.obter(projeto, utilizador).orElseThrow(() -> ErroApi.naoEncontrado("Projeto não encontrado"));
    }

    /** O utilizador autenticado fica Gestor; o cliente nunca indica o dono. */
    @Transactional
    public Projeto criar(long utilizador, String nome, String descricao) {
        long id = projetos.criar(nomeValido(nome), descricaoValida(descricao), utilizador);
        return obter(id, utilizador);
    }

    @Transactional
    public Projeto editar(long projeto, long utilizador, EdicaoProjeto edicao) {
        acesso.exigirGestor(projeto, utilizador);
        if (edicao.vazia()) {
            throw ErroApi.validacao("nome", "Indica pelo menos um campo a alterar");
        }
        if (edicao.alterarNome()) {
            projetos.alterarNome(projeto, nomeValido(edicao.nome()));
        }
        if (edicao.alterarDescricao()) {
            projetos.alterarDescricao(projeto, descricaoValida(edicao.descricao()));
        }
        return obter(projeto, utilizador);
    }

    @Transactional
    public void eliminar(long projeto, long utilizador) {
        acesso.exigirGestor(projeto, utilizador);
        projetos.eliminar(projeto);
    }

    @Transactional(readOnly = true)
    public List<Membro> membros(long projeto, long utilizador) {
        acesso.exigirMembro(projeto, utilizador);
        return projetos.membros(projeto);
    }

    @Transactional
    public Membro adicionarMembro(long projeto, long utilizador, String email) {
        acesso.exigirGestor(projeto, utilizador);
        long novo = projetos.utilizadorPorEmail(email.trim())
                .orElseThrow(() -> ErroApi.regraDeNegocio("/erros/utilizador-inexistente",
                        "Não existe utilizador com este email"));
        if (projetos.membro(projeto, novo).isPresent()) {
            throw ErroApi.conflito("Este utilizador já é membro do projeto");
        }
        try {
            projetos.adicionarMembro(projeto, novo);
        } catch (DuplicateKeyException e) {
            // Dois pedidos ao mesmo tempo: a chave primária impede o duplicado
            throw ErroApi.conflito("Este utilizador já é membro do projeto");
        }
        return projetos.membro(projeto, novo).orElseThrow();
    }

    @Transactional
    public void removerMembro(long projeto, long utilizador, long alvo) {
        acesso.exigirGestor(projeto, utilizador);
        if (projetos.gestor(projeto) == alvo) {
            throw ErroApi.conflito("O gestor não pode ser removido do projeto");
        }
        if (projetos.removerMembro(projeto, alvo) == 0) {
            throw ErroApi.naoEncontrado("Membro não encontrado");
        }
    }

    private static String nomeValido(String nome) {
        if (nome == null || nome.isBlank()) {
            throw ErroApi.validacao("nome", "O nome é obrigatório");
        }
        String limpo = nome.trim();
        if (limpo.length() > MAX_NOME) {
            throw ErroApi.validacao("nome", "O nome tem no máximo " + MAX_NOME + " caracteres");
        }
        return limpo;
    }

    /** Descrição vazia ou só com espaços fica sem descrição. */
    private static String descricaoValida(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return null;
        }
        String limpa = descricao.trim();
        if (limpa.length() > MAX_DESCRICAO) {
            throw ErroApi.validacao("descricao", "A descrição tem no máximo " + MAX_DESCRICAO + " caracteres");
        }
        return limpa;
    }
}
