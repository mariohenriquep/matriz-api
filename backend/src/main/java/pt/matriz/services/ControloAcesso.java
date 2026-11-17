package pt.matriz.services;

import org.springframework.stereotype.Component;

import pt.matriz.domain.Papel;
import pt.matriz.domain.Plano;
import pt.matriz.domain.Utilizador;
import pt.matriz.exceptions.ErroApi;
import pt.matriz.repositories.ProjetoRepositorio;
import pt.matriz.repositories.UtilizadorRepositorio;

/**
 * Regras de acesso de {@code docs/implementation-plan.md} §3, num só sítio. Todas as operações sobre projetos,
 * tarefas, comentários e atribuições passam por aqui antes de ler ou alterar dados.
 *
 * <ul>
 *   <li>Quem não é membro recebe 404, como se o recurso não existisse (CE-07).</li>
 *   <li>Um membro sem o papel necessário recebe 403.</li>
 *   <li>Alterar um projeto arquivado, ou as suas tarefas, dá 409 (os métodos {@code ...Ativo}).</li>
 *   <li>Criar projetos depende do plano da conta: o gratuito recebe 403 (versão 0.8.0).</li>
 * </ul>
 */
@Component
public class ControloAcesso {

    private final ProjetoRepositorio projetos;
    private final UtilizadorRepositorio utilizadores;

    public ControloAcesso(ProjetoRepositorio projetos, UtilizadorRepositorio utilizadores) {
        this.projetos = projetos;
        this.utilizadores = utilizadores;
    }

    /** 403 se o plano da conta não incluir criar projetos (as contas do registo público). */
    public void exigirPodeCriarProjetos(long utilizadorId) {
        boolean pode = utilizadores.findById(utilizadorId).map(Utilizador::getPlano).map(Plano::podeCriarProjetos)
                .orElse(false);
        if (!pode) {
            throw ErroApi.planoSemAcesso("O teu plano não inclui criar projetos. Pede a um gestor que te adicione "
                    + "a um projeto");
        }
    }

    /** Devolve o papel do utilizador no projeto, ou 404 se o projeto não existir ou ele não for membro. */
    public Papel exigirMembro(long projetoId, long utilizadorId) {
        return projetos.papel(projetoId, utilizadorId)
                .orElseThrow(() -> ErroApi.naoEncontrado("Projeto não encontrado"));
    }

    /** Só o Gestor: 404 para quem não é membro, 403 para os outros membros. */
    public void exigirGestor(long projetoId, long utilizadorId) {
        if (exigirMembro(projetoId, utilizadorId) != Papel.GESTOR) {
            throw ErroApi.semPermissao("Só o gestor do projeto pode fazer esta operação");
        }
    }

    /** Como {@link #exigirMembro}, e 409 se o projeto estiver arquivado (só leitura). */
    public Papel exigirMembroAtivo(long projetoId, long utilizadorId) {
        Papel papel = exigirMembro(projetoId, utilizadorId);
        exigirAtivo(projetoId);
        return papel;
    }

    /** Como {@link #exigirGestor}, e 409 se o projeto estiver arquivado. */
    public void exigirGestorAtivo(long projetoId, long utilizadorId) {
        exigirGestor(projetoId, utilizadorId);
        exigirAtivo(projetoId);
    }

    /** Como {@link #exigirMembroDaTarefa}, e 409 se o projeto da tarefa estiver arquivado. */
    public long exigirMembroDaTarefaAtiva(long tarefaId, long utilizadorId) {
        long projeto = exigirMembroDaTarefa(tarefaId, utilizadorId);
        exigirAtivo(projeto);
        return projeto;
    }

    private void exigirAtivo(long projetoId) {
        if (projetos.arquivado(projetoId)) {
            throw ErroApi.projetoArquivado();
        }
    }

    /**
     * Para as rotas {@code /tarefas/{id}/...}: devolve o projeto da tarefa se o utilizador for membro.
     * Uma tarefa de um projeto alheio dá o mesmo 404 que uma tarefa inexistente.
     */
    public long exigirMembroDaTarefa(long tarefaId, long utilizadorId) {
        return projetos.projetoDaTarefa(tarefaId, utilizadorId)
                .orElseThrow(() -> ErroApi.naoEncontrado("Tarefa não encontrada"));
    }
}
