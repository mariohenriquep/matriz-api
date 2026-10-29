package pt.matriz.comum;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contas e projetos de demonstração (Ficha 03, Exercício 2).
 *
 * <p>Só corre se a base de dados não tiver nenhum utilizador, por isso nunca apaga nem duplica dados.
 * Os três projetos dão acessos cruzados: cada utilizador é Gestor de um, membro de outro e não vê o terceiro.
 *
 * <ul>
 *   <li>Website institucional: Gestora Ana, membro Rui (Joana não tem acesso)</li>
 *   <li>App móvel: Gestor Rui, membro Joana (Ana não tem acesso)</li>
 *   <li>Formação interna: Gestora Joana, membro Ana (Rui não tem acesso)</li>
 * </ul>
 */
@Component
@ConditionalOnProperty(name = "matriz.seed.ativo", havingValue = "true")
public class DadosDemonstracao implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosDemonstracao.class);

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwords;
    private final String password;

    public DadosDemonstracao(JdbcTemplate jdbc, PasswordEncoder passwords,
            @Value("${matriz.seed.password:}") String password) {
        this.jdbc = jdbc;
        this.passwords = passwords;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Long utilizadores = jdbc.queryForObject("SELECT count(*) FROM utilizador", Long.class);
        if (utilizadores != null && utilizadores > 0) {
            log.info("Seed ignorado: a base de dados já tem {} utilizador(es).", utilizadores);
            return;
        }
        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "MATRIZ_SEED_PASSWORD não está definida. Define-a no .env ou desativa o seed com MATRIZ_SEED_ATIVO=false.");
        }

        String hash = passwords.encode(password);
        long ana = criarUtilizador("Ana Silva", "ana@matriz.local", hash);
        long rui = criarUtilizador("Rui Costa", "rui@matriz.local", hash);
        long joana = criarUtilizador("Joana Lopes", "joana@matriz.local", hash);

        LocalDate hoje = LocalDate.now();

        long website = criarProjeto("Website institucional",
                "Novo website da empresa, com páginas institucionais e formulário de contacto.", ana, List.of(rui));
        criarTarefa(website, "Definir o mapa do site", null, "CONCLUIDA", "ALTA", ana, hoje.minusDays(10), List.of(ana));
        long maquetas = criarTarefa(website, "Criar as maquetas da página inicial",
                "Versões para computador e telemóvel.", "EM_CURSO", "ALTA", ana, hoje.minusDays(2), List.of(rui, ana));
        criarTarefa(website, "Escrever os textos da secção Sobre", null, "POR_FAZER", "MEDIA", rui, hoje.plusDays(14), List.of());
        criarComentario(maquetas, rui, "A primeira versão está pronta para revisão.");
        criarComentario(maquetas, ana, "Obrigada. Vejo hoje à tarde.");

        long app = criarProjeto("App móvel", "Aplicação para clientes consultarem as encomendas.", rui, List.of(joana));
        criarTarefa(app, "Levantar os requisitos com o cliente", null, "EM_CURSO", "MEDIA", rui, hoje.plusDays(5), List.of(joana));
        criarTarefa(app, "Configurar as notificações push", null, "POR_FAZER", "BAIXA", joana, null, List.of());

        long formacao = criarProjeto("Formação interna", null, joana, List.of(ana));
        criarTarefa(formacao, "Preparar a sessão sobre Git", "Exemplos com branches e pull requests.",
                "POR_FAZER", "ALTA", joana, hoje.plusDays(7), List.of(ana));

        log.info("Seed criado: 3 utilizadores, 3 projetos e 6 tarefas de demonstração.");
    }

    private long criarUtilizador(String nome, String email, String hash) {
        return jdbc.queryForObject(
                "INSERT INTO utilizador (nome, email, password_hash) VALUES (?, ?, ?) RETURNING id",
                Long.class, nome, email, hash);
    }

    private long criarProjeto(String nome, String descricao, long gestor, List<Long> membros) {
        long id = jdbc.queryForObject(
                "INSERT INTO projeto (nome, descricao, gestor_id) VALUES (?, ?, ?) RETURNING id",
                Long.class, nome, descricao, gestor);
        jdbc.update("INSERT INTO membro_projeto (projeto_id, utilizador_id) VALUES (?, ?)", id, gestor);
        for (long membro : membros) {
            jdbc.update("INSERT INTO membro_projeto (projeto_id, utilizador_id) VALUES (?, ?)", id, membro);
        }
        return id;
    }

    private long criarTarefa(long projeto, String titulo, String descricao, String estado, String prioridade,
            long criador, LocalDate dataLimite, List<Long> responsaveis) {
        long id = jdbc.queryForObject("""
                INSERT INTO tarefa (projeto_id, titulo, descricao, estado, prioridade, criador_id, data_limite)
                VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id""",
                Long.class, projeto, titulo, descricao, estado, prioridade, criador, dataLimite);
        for (long responsavel : responsaveis) {
            jdbc.update("INSERT INTO tarefa_responsavel (tarefa_id, utilizador_id) VALUES (?, ?)", id, responsavel);
        }
        return id;
    }

    private void criarComentario(long tarefa, long autor, String conteudo) {
        jdbc.update("INSERT INTO comentario (tarefa_id, autor_id, conteudo, criado_em) VALUES (?, ?, ?, clock_timestamp())", tarefa, autor, conteudo);
    }
}
