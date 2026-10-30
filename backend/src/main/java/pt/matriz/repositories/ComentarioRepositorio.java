package pt.matriz.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import pt.matriz.domain.Comentario;
import pt.matriz.domain.Pessoa;

/** Comentários das tarefas, em SQL explícito. Não há operações de editar nem apagar (decisão do MVP). */
@Repository
public class ComentarioRepositorio {

    private static final String SELECAO = """
            SELECT c.id, c.tarefa_id, c.conteudo, c.criado_em, c.autor_id, u.nome AS autor_nome
            FROM comentario c JOIN utilizador u ON u.id = c.autor_id
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public ComentarioRepositorio(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Do mais antigo para o mais recente. */
    public List<Comentario> listar(long tarefa) {
        return jdbc.query(SELECAO + "WHERE c.tarefa_id = :tarefa ORDER BY c.criado_em, c.id",
                new MapSqlParameterSource("tarefa", tarefa), ComentarioRepositorio::linha);
    }

    public Optional<Comentario> obter(long comentario) {
        return jdbc.query(SELECAO + "WHERE c.id = :id", new MapSqlParameterSource("id", comentario),
                ComentarioRepositorio::linha).stream().findFirst();
    }

    /** A data é a da base de dados ({@code clock_timestamp()}), nunca a do cliente. */
    public long criar(long tarefa, long autor, String conteudo) {
        return jdbc.queryForObject("""
                INSERT INTO comentario (tarefa_id, autor_id, conteudo, criado_em)
                VALUES (:tarefa, :autor, :conteudo, clock_timestamp()) RETURNING id""",
                new MapSqlParameterSource("tarefa", tarefa).addValue("autor", autor).addValue("conteudo", conteudo),
                Long.class);
    }

    private static Comentario linha(ResultSet linha, int n) throws SQLException {
        return new Comentario(linha.getLong("id"), linha.getLong("tarefa_id"), linha.getString("conteudo"),
                new Pessoa(linha.getLong("autor_id"), linha.getString("autor_nome")),
                linha.getObject("criado_em", OffsetDateTime.class));
    }
}
