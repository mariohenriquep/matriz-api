package pt.matriz.projetos;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import pt.matriz.comum.ErroApi;

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

    private final JdbcTemplate jdbc;

    public ControloAcesso(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Devolve o papel do utilizador no projeto, ou 404 se o projeto não existir ou ele não for membro. */
    public Papel exigirMembro(long projetoId, long utilizadorId) {
        List<Boolean> gestor = jdbc.queryForList("""
                SELECT p.gestor_id = m.utilizador_id
                FROM projeto p JOIN membro_projeto m ON m.projeto_id = p.id
                WHERE p.id = ? AND m.utilizador_id = ?""", Boolean.class, projetoId, utilizadorId);
        if (gestor.isEmpty()) {
            throw ErroApi.naoEncontrado("Projeto não encontrado");
        }
        return gestor.getFirst() ? Papel.GESTOR : Papel.MEMBRO;
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
        List<Long> projeto = jdbc.queryForList("""
                SELECT t.projeto_id
                FROM tarefa t JOIN membro_projeto m ON m.projeto_id = t.projeto_id
                WHERE t.id = ? AND m.utilizador_id = ?""", Long.class, tarefaId, utilizadorId);
        if (projeto.isEmpty()) {
            throw ErroApi.naoEncontrado("Tarefa não encontrada");
        }
        return projeto.getFirst();
    }
}
