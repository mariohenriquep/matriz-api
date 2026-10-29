package pt.matriz.services;

import org.springframework.stereotype.Component;

import pt.matriz.domain.Papel;
import pt.matriz.exceptions.ErroApi;
import pt.matriz.repositories.ProjetoRepositorio;

/**
 * Regras de acesso de {@code docs/implementation-plan.md} §3, num só sítio. Todas as operações sobre projetos,
 * tarefas, comentários e atribuições passam por aqui antes de ler ou alterar dados.
 *
 * <ul>
 *   <li>Quem não é membro recebe 404, como se o recurso não existisse (CE-07).</li>
 *   <li>Um membro sem o papel necessário recebe 403.</li>
 * </ul>
 */
@Component
public class ControloAcesso {

    private final ProjetoRepositorio projetos;

    public ControloAcesso(ProjetoRepositorio projetos) {
        this.projetos = projetos;
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

    /**
     * Para as rotas {@code /tarefas/{id}/...}: devolve o projeto da tarefa se o utilizador for membro.
     * Uma tarefa de um projeto alheio dá o mesmo 404 que uma tarefa inexistente.
     */
    public long exigirMembroDaTarefa(long tarefaId, long utilizadorId) {
        return projetos.projetoDaTarefa(tarefaId, utilizadorId)
                .orElseThrow(() -> ErroApi.naoEncontrado("Tarefa não encontrada"));
    }
}
