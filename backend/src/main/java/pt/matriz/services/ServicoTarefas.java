package pt.matriz.services;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.matriz.domain.CriacaoTarefa;
import pt.matriz.domain.EdicaoTarefa;
import pt.matriz.domain.FiltroTarefas;
import pt.matriz.domain.Papel;
import pt.matriz.domain.Prioridade;
import pt.matriz.domain.Tarefa;
import pt.matriz.exceptions.ErroApi;
import pt.matriz.repositories.TarefaRepositorio;
import pt.matriz.repositories.TarefaRepositorio.Coluna;

/** Tarefas de um projeto (Ficha 03, Ex. 5). Todas as operações exigem ser membro do projeto. */
@Service
public class ServicoTarefas {

    static final int MAX_TITULO = 200;
    static final int MAX_DESCRICAO = 2000;

    private final TarefaRepositorio tarefas;
    private final ControloAcesso acesso;

    public ServicoTarefas(TarefaRepositorio tarefas, ControloAcesso acesso) {
        this.tarefas = tarefas;
        this.acesso = acesso;
    }

    @Transactional(readOnly = true)
    public List<Tarefa> listar(long projeto, long utilizador, FiltroTarefas filtro) {
        acesso.exigirMembro(projeto, utilizador);
        return tarefas.listar(projeto, filtro);
    }

    @Transactional(readOnly = true)
    public Tarefa obter(long tarefa, long utilizador) {
        acesso.exigirMembroDaTarefa(tarefa, utilizador);
        return tarefas.obter(tarefa).orElseThrow(() -> ErroApi.naoEncontrado("Tarefa não encontrada"));
    }

    /** Estado inicial {@code POR_FAZER}; o criador é sempre o utilizador autenticado. */
    @Transactional
    public Tarefa criar(long projeto, long utilizador, CriacaoTarefa nova) {
        acesso.exigirMembro(projeto, utilizador);
        List<Long> responsaveis = responsaveisValidos(projeto, nova.responsavelIds());
        Prioridade prioridade = nova.prioridade() == null ? Prioridade.MEDIA : nova.prioridade();
        long id = tarefas.criar(projeto, tituloValido(nova.titulo()), descricaoValida(nova.descricao()), prioridade,
                nova.dataLimite(), utilizador);
        responsaveis.forEach(responsavel -> tarefas.atribuir(id, responsavel));
        return tarefas.obter(id).orElseThrow();
    }

    /** Qualquer membro edita, incluindo estado e prioridade; qualquer transição de estado é permitida (P-04). */
    @Transactional
    public Tarefa editar(long tarefa, long utilizador, EdicaoTarefa edicao) {
        acesso.exigirMembroDaTarefa(tarefa, utilizador);
        if (edicao.vazia()) {
            throw ErroApi.validacao("titulo", "Indica pelo menos um campo a alterar");
        }
        if (edicao.alterarTitulo()) {
            tarefas.alterar(tarefa, Coluna.TITULO, tituloValido(edicao.titulo()));
        }
        if (edicao.alterarDescricao()) {
            tarefas.alterar(tarefa, Coluna.DESCRICAO, descricaoValida(edicao.descricao()));
        }
        if (edicao.alterarEstado()) {
            tarefas.alterar(tarefa, Coluna.ESTADO, obrigatorio(edicao.estado(), "estado", "O estado é obrigatório"));
        }
        if (edicao.alterarPrioridade()) {
            tarefas.alterar(tarefa, Coluna.PRIORIDADE,
                    obrigatorio(edicao.prioridade(), "prioridade", "A prioridade é obrigatória"));
        }
        if (edicao.alterarDataLimite()) {
            tarefas.alterar(tarefa, Coluna.DATA_LIMITE, edicao.dataLimite());
        }
        return tarefas.obter(tarefa).orElseThrow();
    }

    /** Só o criador da tarefa ou o Gestor do projeto. */
    @Transactional
    public void eliminar(long tarefa, long utilizador) {
        long projeto = acesso.exigirMembroDaTarefa(tarefa, utilizador);
        boolean gestor = acesso.exigirMembro(projeto, utilizador) == Papel.GESTOR;
        if (!gestor && tarefas.criador(tarefa) != utilizador) {
            throw ErroApi.semPermissao("Só quem criou a tarefa ou o gestor do projeto a pode eliminar");
        }
        tarefas.eliminar(tarefa);
    }

    /**
     * Junta um responsável (Ex. 6). Qualquer membro pode atribuir; o responsável tem de ser membro do projeto.
     * Um id inexistente dá a mesma resposta que um não membro, para não revelar que contas existem.
     */
    @Transactional
    public Tarefa atribuir(long tarefa, long utilizador, long responsavel) {
        long projeto = acesso.exigirMembroDaTarefa(tarefa, utilizador);
        if (!tarefas.naoMembros(projeto, List.of(responsavel)).isEmpty()) {
            throw ErroApi.regraDeNegocio("/erros/responsavel-nao-membro", "O responsável tem de ser membro do projeto");
        }
        if (tarefas.eResponsavel(tarefa, responsavel)) {
            throw ErroApi.conflito("Este utilizador já é responsável pela tarefa");
        }
        try {
            tarefas.atribuir(tarefa, responsavel);
        } catch (DuplicateKeyException e) {
            // Dois pedidos ao mesmo tempo: a chave primária impede o duplicado
            throw ErroApi.conflito("Este utilizador já é responsável pela tarefa");
        }
        return tarefas.obter(tarefa).orElseThrow();
    }

    @Transactional
    public void removerResponsavel(long tarefa, long utilizador, long responsavel) {
        acesso.exigirMembroDaTarefa(tarefa, utilizador);
        if (tarefas.removerResponsavel(tarefa, responsavel) == 0) {
            throw ErroApi.naoEncontrado("Este utilizador não é responsável pela tarefa");
        }
    }

    /** Sem repetições e todos membros do projeto (RN-02). */
    private List<Long> responsaveisValidos(long projeto, List<Long> ids) {
        Set<Long> unicos = new LinkedHashSet<>(ids);
        if (unicos.size() != ids.size()) {
            throw ErroApi.validacao("responsavelIds", "O mesmo responsável foi indicado mais de uma vez");
        }
        List<Long> lista = new ArrayList<>(unicos);
        if (!tarefas.naoMembros(projeto, lista).isEmpty()) {
            throw ErroApi.regraDeNegocio("/erros/responsavel-nao-membro", "O responsável tem de ser membro do projeto");
        }
        return lista;
    }

    private static String tituloValido(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw ErroApi.validacao("titulo", "O título é obrigatório");
        }
        String limpo = titulo.trim();
        if (limpo.length() > MAX_TITULO) {
            throw ErroApi.validacao("titulo", "O título tem no máximo " + MAX_TITULO + " caracteres");
        }
        return limpo;
    }

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

    private static <T> T obrigatorio(T valor, String campo, String mensagem) {
        if (valor == null) {
            throw ErroApi.validacao(campo, mensagem);
        }
        return valor;
    }
}
