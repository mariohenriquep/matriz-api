package pt.matriz.projetos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Acesso à base de dados para projetos e membros, em SQL explícito: as listagens precisam de agregados
 * (contagens por estado) e do papel do utilizador, que se escrevem melhor numa só consulta.
 */
@Repository
public class ProjetoRepositorio {

    private static final String SELECAO_PROJETO = """
            SELECT p.id, p.nome, p.descricao, p.gestor_id, g.nome AS gestor_nome, p.criado_em, p.atualizado_em,
                   count(t.id) FILTER (WHERE t.estado = 'POR_FAZER') AS por_fazer,
                   count(t.id) FILTER (WHERE t.estado = 'EM_CURSO') AS em_curso,
                   count(t.id) FILTER (WHERE t.estado = 'CONCLUIDA') AS concluidas
            FROM projeto p
            JOIN membro_projeto m ON m.projeto_id = p.id AND m.utilizador_id = :utilizador
            JOIN utilizador g ON g.id = p.gestor_id
            LEFT JOIN tarefa t ON t.projeto_id = p.id
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public ProjetoRepositorio(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Só os projetos de que o utilizador é membro; {@code pesquisa} nula devolve todos. */
    public List<ProjetoVista> listar(long utilizador, String pesquisa) {
        MapSqlParameterSource parametros = new MapSqlParameterSource("utilizador", utilizador);
        String filtro = "";
        if (pesquisa != null) {
            filtro = "WHERE unaccent(lower(p.nome)) LIKE '%' || unaccent(lower(:pesquisa)) || '%' ESCAPE '\\' ";
            parametros.addValue("pesquisa", escaparLike(pesquisa));
        }
        return jdbc.query(SELECAO_PROJETO + filtro + "GROUP BY p.id, g.nome ORDER BY lower(p.nome), p.id",
                parametros, (linha, n) -> projeto(linha, utilizador));
    }

    public Optional<ProjetoVista> obter(long projeto, long utilizador) {
        MapSqlParameterSource parametros = new MapSqlParameterSource("utilizador", utilizador)
                .addValue("projeto", projeto);
        return jdbc.query(SELECAO_PROJETO + "WHERE p.id = :projeto GROUP BY p.id, g.nome", parametros,
                (linha, n) -> projeto(linha, utilizador)).stream().findFirst();
    }

    /** Cria o projeto com o Gestor também como membro (os membros incluem sempre o Gestor). */
    public long criar(String nome, String descricao, long gestor) {
        Long id = jdbc.queryForObject(
                "INSERT INTO projeto (nome, descricao, gestor_id) VALUES (:nome, :descricao, :gestor) RETURNING id",
                new MapSqlParameterSource("nome", nome).addValue("descricao", descricao).addValue("gestor", gestor),
                Long.class);
        jdbc.update("INSERT INTO membro_projeto (projeto_id, utilizador_id) VALUES (:projeto, :gestor)",
                new MapSqlParameterSource("projeto", id).addValue("gestor", gestor));
        return id;
    }

    public void alterarNome(long projeto, String nome) {
        jdbc.update("UPDATE projeto SET nome = :nome, atualizado_em = now() WHERE id = :projeto",
                new MapSqlParameterSource("nome", nome).addValue("projeto", projeto));
    }

    public void alterarDescricao(long projeto, String descricao) {
        jdbc.update("UPDATE projeto SET descricao = :descricao, atualizado_em = now() WHERE id = :projeto",
                new MapSqlParameterSource("descricao", descricao).addValue("projeto", projeto));
    }

    /** Membros, tarefas, atribuições e comentários saem com o projeto ({@code ON DELETE CASCADE}, RN-10). */
    public void eliminar(long projeto) {
        jdbc.update("DELETE FROM projeto WHERE id = :projeto", new MapSqlParameterSource("projeto", projeto));
    }

    /** Gestor primeiro, depois por nome. */
    public List<MembroVista> membros(long projeto) {
        return jdbc.query("""
                SELECT u.id, u.nome, u.email, u.id = p.gestor_id AS gestor
                FROM membro_projeto m
                JOIN utilizador u ON u.id = m.utilizador_id
                JOIN projeto p ON p.id = m.projeto_id
                WHERE m.projeto_id = :projeto
                ORDER BY u.id = p.gestor_id DESC, lower(u.nome), u.id""",
                new MapSqlParameterSource("projeto", projeto),
                (linha, n) -> new MembroVista(linha.getLong("id"), linha.getString("nome"), linha.getString("email"),
                        linha.getBoolean("gestor") ? Papel.GESTOR : Papel.MEMBRO));
    }

    public Optional<MembroVista> membro(long projeto, long utilizador) {
        return membros(projeto).stream().filter(membro -> membro.id() == utilizador).findFirst();
    }

    public Optional<Long> utilizadorPorEmail(String email) {
        return jdbc.queryForList("SELECT id FROM utilizador WHERE lower(email) = lower(:email)",
                new MapSqlParameterSource("email", email), Long.class).stream().findFirst();
    }

    public long gestor(long projeto) {
        return jdbc.queryForObject("SELECT gestor_id FROM projeto WHERE id = :projeto",
                new MapSqlParameterSource("projeto", projeto), Long.class);
    }

    public void adicionarMembro(long projeto, long utilizador) {
        jdbc.update("INSERT INTO membro_projeto (projeto_id, utilizador_id) VALUES (:projeto, :utilizador)",
                new MapSqlParameterSource("projeto", projeto).addValue("utilizador", utilizador));
    }

    /** Remove o membro e as suas atribuições nas tarefas deste projeto (RN-03); as tarefas ficam. */
    public int removerMembro(long projeto, long utilizador) {
        MapSqlParameterSource parametros = new MapSqlParameterSource("projeto", projeto)
                .addValue("utilizador", utilizador);
        jdbc.update("""
                DELETE FROM tarefa_responsavel
                WHERE utilizador_id = :utilizador
                  AND tarefa_id IN (SELECT id FROM tarefa WHERE projeto_id = :projeto)""", parametros);
        return jdbc.update("DELETE FROM membro_projeto WHERE projeto_id = :projeto AND utilizador_id = :utilizador",
                parametros);
    }

    private static ProjetoVista projeto(ResultSet linha, long utilizador) throws SQLException {
        long gestor = linha.getLong("gestor_id");
        return new ProjetoVista(linha.getLong("id"), linha.getString("nome"), linha.getString("descricao"), gestor,
                linha.getString("gestor_nome"), gestor == utilizador ? Papel.GESTOR : Papel.MEMBRO,
                linha.getLong("por_fazer"), linha.getLong("em_curso"), linha.getLong("concluidas"),
                linha.getObject("criado_em", OffsetDateTime.class), linha.getObject("atualizado_em", OffsetDateTime.class));
    }

    /** O texto pesquisado é literal: {@code %} e {@code _} não funcionam como curingas. */
    static String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
