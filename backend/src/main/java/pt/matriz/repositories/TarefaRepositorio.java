package pt.matriz.repositories;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import pt.matriz.domain.EstadoTarefa;
import pt.matriz.domain.FiltroTarefas;
import pt.matriz.domain.Pessoa;
import pt.matriz.domain.Prioridade;
import pt.matriz.domain.Tarefa;

/** Acesso à base de dados para tarefas, em SQL explícito como em {@code ProjetoRepositorio}. */
@Repository
public class TarefaRepositorio {

    private static final String SELECAO = """
            SELECT t.id, t.projeto_id, t.titulo, t.descricao, t.estado, t.prioridade, t.data_limite,
                   t.criado_em, t.atualizado_em, t.criador_id, c.nome AS criador_nome,
                   (SELECT count(*) FROM comentario co WHERE co.tarefa_id = t.id) AS total_comentarios
            FROM tarefa t
            JOIN utilizador c ON c.id = t.criador_id
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public TarefaRepositorio(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Mais recentes primeiro. */
    public List<Tarefa> listar(long projeto, FiltroTarefas filtro) {
        StringBuilder sql = new StringBuilder(SELECAO).append("WHERE t.projeto_id = :projeto ");
        MapSqlParameterSource parametros = new MapSqlParameterSource("projeto", projeto);
        if (filtro.estado() != null) {
            sql.append("AND t.estado = :estado ");
            parametros.addValue("estado", filtro.estado().name());
        }
        if (filtro.prioridade() != null) {
            sql.append("AND t.prioridade = :prioridade ");
            parametros.addValue("prioridade", filtro.prioridade().name());
        }
        if (filtro.responsavelId() != null) {
            sql.append("AND EXISTS (SELECT 1 FROM tarefa_responsavel r WHERE r.tarefa_id = t.id AND r.utilizador_id = :responsavel) ");
            parametros.addValue("responsavel", filtro.responsavelId());
        }
        sql.append("ORDER BY t.criado_em DESC, t.id DESC");
        return comResponsaveis(jdbc.query(sql.toString(), parametros, TarefaRepositorio::linha));
    }

    public Optional<Tarefa> obter(long tarefa) {
        return comResponsaveis(jdbc.query(SELECAO + "WHERE t.id = :tarefa", new MapSqlParameterSource("tarefa", tarefa),
                TarefaRepositorio::linha)).stream().findFirst();
    }

    public long criar(long projeto, String titulo, String descricao, Prioridade prioridade, LocalDate dataLimite,
            long criador) {
        return jdbc.queryForObject("""
                INSERT INTO tarefa (projeto_id, titulo, descricao, prioridade, data_limite, criador_id)
                VALUES (:projeto, :titulo, :descricao, :prioridade, :dataLimite, :criador) RETURNING id""",
                new MapSqlParameterSource("projeto", projeto).addValue("titulo", titulo)
                        .addValue("descricao", descricao).addValue("prioridade", prioridade.name())
                        .addValue("dataLimite", dataLimite == null ? null : Date.valueOf(dataLimite))
                        .addValue("criador", criador),
                Long.class);
    }

    public void atribuir(long tarefa, long utilizador) {
        jdbc.update("INSERT INTO tarefa_responsavel (tarefa_id, utilizador_id) VALUES (:tarefa, :utilizador)",
                new MapSqlParameterSource("tarefa", tarefa).addValue("utilizador", utilizador));
    }

    /**
     * Altera uma coluna e o {@code atualizado_em}. A coluna vem sempre de código, nunca do cliente.
     * Enums são guardados pelo nome e datas como {@code DATE}.
     */
    public void alterar(long tarefa, Coluna coluna, Object valor) {
        Object valorSql = switch (valor) {
            case Enum<?> e -> e.name();
            case LocalDate data -> Date.valueOf(data);
            case null -> null;
            default -> valor;
        };
        jdbc.update("UPDATE tarefa SET " + coluna.nome + " = :valor, atualizado_em = now() WHERE id = :tarefa",
                new MapSqlParameterSource("tarefa", tarefa).addValue("valor", valorSql));
    }

    /** Atribuições e comentários saem com a tarefa ({@code ON DELETE CASCADE}, RN-10). */
    public void eliminar(long tarefa) {
        jdbc.update("DELETE FROM tarefa WHERE id = :tarefa", new MapSqlParameterSource("tarefa", tarefa));
    }

    public long criador(long tarefa) {
        return jdbc.queryForObject("SELECT criador_id FROM tarefa WHERE id = :tarefa",
                new MapSqlParameterSource("tarefa", tarefa), Long.class);
    }

    /** Ids, de entre os indicados, que não são membros do projeto. */
    public List<Long> naoMembros(long projeto, Collection<Long> utilizadores) {
        if (utilizadores.isEmpty()) {
            return List.of();
        }
        List<Long> membros = jdbc.queryForList(
                "SELECT utilizador_id FROM membro_projeto WHERE projeto_id = :projeto AND utilizador_id IN (:ids)",
                new MapSqlParameterSource("projeto", projeto).addValue("ids", utilizadores), Long.class);
        return utilizadores.stream().filter(id -> !membros.contains(id)).toList();
    }

    public enum Coluna {
        TITULO("titulo"), DESCRICAO("descricao"), ESTADO("estado"), PRIORIDADE("prioridade"), DATA_LIMITE("data_limite");

        private final String nome;

        Coluna(String nome) {
            this.nome = nome;
        }
    }

    /** Junta os responsáveis de todas as tarefas numa só consulta (sem uma consulta por tarefa). */
    private List<Tarefa> comResponsaveis(List<Tarefa> tarefas) {
        if (tarefas.isEmpty()) {
            return tarefas;
        }
        Map<Long, List<Pessoa>> porTarefa = new HashMap<>();
        jdbc.query("""
                SELECT r.tarefa_id, u.id, u.nome
                FROM tarefa_responsavel r JOIN utilizador u ON u.id = r.utilizador_id
                WHERE r.tarefa_id IN (:ids)
                ORDER BY lower(u.nome), u.id""",
                new MapSqlParameterSource("ids", tarefas.stream().map(Tarefa::id).toList()),
                linha -> {
                    porTarefa.computeIfAbsent(linha.getLong("tarefa_id"), id -> new ArrayList<>())
                            .add(new Pessoa(linha.getLong("id"), linha.getString("nome")));
                });
        return tarefas.stream().map(t -> t.comResponsaveis(porTarefa.getOrDefault(t.id(), List.of()))).toList();
    }

    private static Tarefa linha(ResultSet linha, int n) throws SQLException {
        Date dataLimite = linha.getDate("data_limite");
        return new Tarefa(linha.getLong("id"), linha.getLong("projeto_id"), linha.getString("titulo"),
                linha.getString("descricao"), EstadoTarefa.valueOf(linha.getString("estado")),
                Prioridade.valueOf(linha.getString("prioridade")), List.of(),
                new Pessoa(linha.getLong("criador_id"), linha.getString("criador_nome")),
                dataLimite == null ? null : dataLimite.toLocalDate(), linha.getLong("total_comentarios"),
                linha.getObject("criado_em", OffsetDateTime.class), linha.getObject("atualizado_em", OffsetDateTime.class));
    }
}
