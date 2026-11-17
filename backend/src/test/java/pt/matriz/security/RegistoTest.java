package pt.matriz.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.ResultActions;

import pt.matriz.ApiTeste;

/**
 * Registo público (versão 0.8.0): a conta nova fica no plano gratuito (não cria projetos, só vê os projetos para
 * que um Gestor a convida) e a resposta não revela se o email já tinha conta. Os dados ficam gravados a sério,
 * por isso as contas criadas (domínio registo.teste) são apagadas no fim de cada teste.
 */
class RegistoTest extends ApiTeste {

    private static final String PASSWORD = "uma password segura";

    @Autowired
    JdbcTemplate jdbc;

    @AfterEach
    void apagarContas() {
        jdbc.update("DELETE FROM membro_projeto WHERE utilizador_id IN "
                + "(SELECT id FROM utilizador WHERE email LIKE '%@registo.teste')");
        jdbc.update("DELETE FROM utilizador WHERE email LIKE '%@registo.teste'");
    }

    private ResultActions registar(String nome, String email, String password) throws Exception {
        return registar(nome, email, password, null);
    }

    private ResultActions registar(String nome, String email, String password, String site) throws Exception {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("nome", nome);
        corpo.put("email", email);
        corpo.put("password", password);
        if (site != null) {
            corpo.put("site", site);
        }
        return mvc.perform(post("/api/v1/auth/registo").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(corpo)));
    }

    private String entrar(String email, String password) throws Exception {
        String resposta = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.utilizador.plano").value("GRATUITO"))
                .andExpect(jsonPath("$.utilizador.podeCriarProjetos").value(false))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resposta).get("token").asText();
    }

    private Integer contas(String email) {
        return jdbc.queryForObject("SELECT count(*) FROM utilizador WHERE lower(email) = lower(?)", Integer.class,
                email);
    }

    @Test
    void criaUmaContaGratuitaQueEntraMasNaoCriaProjetos() throws Exception {
        registar("  Nova Pessoa ", " Nova@Registo.Teste ", PASSWORD)
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.mensagem").value(ControladorRegistoMensagem.TEXTO));

        // Guardada limpa, em minúsculas, com hash bcrypt e plano gratuito
        Map<String, Object> linha = jdbc.queryForMap(
                "SELECT nome, email, password_hash, plano FROM utilizador WHERE email = 'nova@registo.teste'");
        assertThat(linha.get("nome")).isEqualTo("Nova Pessoa");
        assertThat((String) linha.get("password_hash")).startsWith("$2").doesNotContain(PASSWORD);
        assertThat(linha.get("plano")).isEqualTo("GRATUITO");

        String token = entrar("nova@registo.teste", PASSWORD);
        mvc.perform(como(token, get("/api/v1/auth/eu")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plano").value("GRATUITO"))
                .andExpect(jsonPath("$.podeCriarProjetos").value(false));

        // Não vê nenhum projeto e não pode criar um
        mvc.perform(como(token, get("/api/v1/projetos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens.length()").value(0));
        mvc.perform(como(token, post("/api/v1/projetos"), Map.of("nome", "O meu projeto")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value("/erros/plano-sem-acesso"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM projeto WHERE nome = 'O meu projeto'", Integer.class))
                .isZero();
    }

    @Test
    void depoisDeUmGestorOAdicionarVeOProjetoEPodeCriarTarefas() throws Exception {
        registar("Nova Pessoa", "nova@registo.teste", PASSWORD).andExpect(status().isAccepted());
        String tokenAna = token("ana@matriz.local");
        long website = jdbc.queryForObject("SELECT id FROM projeto WHERE nome = 'Website institucional'", Long.class);
        mvc.perform(como(tokenAna, post("/api/v1/projetos/" + website + "/membros"),
                        Map.of("email", "nova@registo.teste")))
                .andExpect(status().isCreated());

        String token = entrar("nova@registo.teste", PASSWORD);
        mvc.perform(como(token, get("/api/v1/projetos")))
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.itens[0].meuPapel").value("MEMBRO"));
        String tarefa = mvc.perform(como(token, post("/api/v1/projetos/" + website + "/tarefas"),
                        Map.of("titulo", "Tarefa da pessoa nova")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        jdbc.update("DELETE FROM tarefa WHERE id = ?", Long.parseLong(json.readTree(tarefa).get("id").asText()));
    }

    @Test
    void comUmEmailJaRegistadoARespostaEAMesmaENadaMuda() throws Exception {
        String hashAna = jdbc.queryForObject("SELECT password_hash FROM utilizador WHERE email = 'ana@matriz.local'",
                String.class);
        String novo = registar("Nova Pessoa", "nova@registo.teste", PASSWORD)
                .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();
        String existente = registar("Intrusa", "ANA@matriz.local", "outra password segura")
                .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();
        String repetido = registar("Outra", "nova@REGISTO.teste", "mais uma password")
                .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();

        assertThat(existente).isEqualTo(novo);
        assertThat(repetido).isEqualTo(novo);
        // A conta da Ana não mudou e não há duplicados
        assertThat(jdbc.queryForObject("SELECT password_hash FROM utilizador WHERE email = 'ana@matriz.local'",
                String.class)).isEqualTo(hashAna);
        assertThat(contas("ana@matriz.local")).isEqualTo(1);
        assertThat(contas("nova@registo.teste")).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT nome FROM utilizador WHERE email = 'nova@registo.teste'",
                String.class)).isEqualTo("Nova Pessoa");
    }

    @Test
    void oCampoEscondidoPreenchidoNaoCriaContaMasRespondeIgual() throws Exception {
        registar("Robô", "robo@registo.teste", PASSWORD, "https://spam.example")
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.mensagem").value(ControladorRegistoMensagem.TEXTO));
        assertThat(contas("robo@registo.teste")).isZero();
    }

    @Test
    void dadosInvalidosDao400NoCampoCerto() throws Exception {
        registar("Nova Pessoa", "nova@registo", PASSWORD)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("email"));
        registar("Nova Pessoa", "nova@registo.teste", "curta")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("password"));
        registar("Nova Pessoa", "nova@registo.teste", "password123")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].mensagem").value("Esta password é demasiado comum. Escolhe outra"));
        registar("", "nova@registo.teste", PASSWORD)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("nome"));
        // Campos a mais (ex.: tentar escolher o plano) são recusados
        mvc.perform(post("/api/v1/auth/registo").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("nome", "Nova", "email", "nova@registo.teste",
                                "password", PASSWORD, "plano", "PROFISSIONAL"))))
                .andExpect(status().isBadRequest());
        assertThat(contas("nova@registo.teste")).isZero();
    }

    @Test
    void asContasQueJaExistiamMantemOPlanoProfissional() throws Exception {
        mvc.perform(como(token("rui@matriz.local"), get("/api/v1/auth/eu")))
                .andExpect(jsonPath("$.plano").value("PROFISSIONAL"))
                .andExpect(jsonPath("$.podeCriarProjetos").value(true));
    }

    /** A mensagem fixa do registo, repetida aqui para o teste não depender de um campo interno. */
    private static final class ControladorRegistoMensagem {
        static final String TEXTO = "Pedido de registo recebido. Se este email ainda não tinha conta, "
                + "a conta foi criada e já podes iniciar sessão.";
    }
}
