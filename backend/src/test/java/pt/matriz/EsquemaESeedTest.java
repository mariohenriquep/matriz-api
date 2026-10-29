package pt.matriz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import pt.matriz.comum.DadosDemonstracao;

/** Verifica as migrations e o seed contra um PostgreSQL real (Ficha 03, Exercício 2). */
class EsquemaESeedTest extends TesteIntegracao {

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    DadosDemonstracao seed;

    @Test
    void migrationsCriamTodasAsTabelas() {
        List<String> tabelas = jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'", String.class);
        assertThat(tabelas).contains("utilizador", "projeto", "membro_projeto", "tarefa", "tarefa_responsavel",
                "comentario");
    }

    @Test
    void seedCriaDadosDeDemonstracaoComHash() {
        assertThat(contar("utilizador")).isEqualTo(3);
        assertThat(contar("projeto")).isEqualTo(3);
        assertThat(contar("tarefa")).isEqualTo(6);

        String hash = jdbc.queryForObject("SELECT password_hash FROM utilizador WHERE email = 'ana@matriz.local'",
                String.class);
        assertThat(hash).isNotEqualTo(PASSWORD_DEMO);
        assertThat(new BCryptPasswordEncoder().matches(PASSWORD_DEMO, hash)).isTrue();
    }

    @Test
    void seedDaAcessosDistintos() {
        assertThat(projetosDe("ana@matriz.local")).containsExactlyInAnyOrder("Website institucional", "Formação interna");
        assertThat(projetosDe("rui@matriz.local")).containsExactlyInAnyOrder("Website institucional", "App móvel");
        assertThat(projetosDe("joana@matriz.local")).containsExactlyInAnyOrder("App móvel", "Formação interna");

        Long gestoresSemLinhaDeMembro = jdbc.queryForObject("""
                SELECT count(*) FROM projeto p
                WHERE NOT EXISTS (SELECT 1 FROM membro_projeto m
                                  WHERE m.projeto_id = p.id AND m.utilizador_id = p.gestor_id)""", Long.class);
        assertThat(gestoresSemLinhaDeMembro).isZero();
    }

    @Test
    void seedNaoDuplicaNumSegundoArranque() throws Exception {
        long antes = contar("utilizador") + contar("projeto") + contar("tarefa") + contar("comentario");
        seed.run(null);
        long depois = contar("utilizador") + contar("projeto") + contar("tarefa") + contar("comentario");
        assertThat(depois).isEqualTo(antes);
    }

    @Test
    void esquemaRecusaEmailRepetidoComMaiusculasDiferentes() {
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO utilizador (nome, email, password_hash) VALUES ('Outra Ana', 'ANA@matriz.local', 'x')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional // a eliminação é desfeita no fim do teste
    void eliminarProjetoNaoDeixaDadosOrfaos() {
        Long projeto = jdbc.queryForObject("SELECT id FROM projeto WHERE nome = 'Website institucional'", Long.class);
        jdbc.update("DELETE FROM projeto WHERE id = ?", projeto);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM tarefa WHERE projeto_id = ?", Long.class, projeto)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM membro_projeto WHERE projeto_id = ?", Long.class, projeto)).isZero();
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM comentario c WHERE NOT EXISTS (SELECT 1 FROM tarefa t WHERE t.id = c.tarefa_id)",
                Long.class)).isZero();
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM tarefa_responsavel r WHERE NOT EXISTS (SELECT 1 FROM tarefa t WHERE t.id = r.tarefa_id)",
                Long.class)).isZero();
    }

    private long contar(String tabela) {
        return jdbc.queryForObject("SELECT count(*) FROM " + tabela, Long.class);
    }

    private List<String> projetosDe(String email) {
        return jdbc.queryForList("""
                SELECT p.nome FROM projeto p
                JOIN membro_projeto m ON m.projeto_id = p.id
                JOIN utilizador u ON u.id = m.utilizador_id
                WHERE u.email = ?""", String.class, email);
    }
}
