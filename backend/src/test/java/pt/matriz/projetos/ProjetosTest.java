package pt.matriz.projetos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import pt.matriz.ApiTeste;

/**
 * Ficha 03, Ex. 4: projetos por HTTP, com utilizadores diferentes. Cada teste corre numa transação desfeita
 * no fim, para os dados do seed ficarem iguais entre testes.
 */
@Transactional
class ProjetosTest extends ApiTeste {

    @Autowired
    JdbcTemplate jdbc;

    String ana;
    String rui;
    String joana;

    @BeforeEach
    void entrar() throws Exception {
        ana = token("ana@matriz.local");
        rui = token("rui@matriz.local");
        joana = token("joana@matriz.local");
    }

    // --- Listar e pesquisar -------------------------------------------------------------------------------

    @Test
    void listaSoOsProjetosDeQueSouMembroComPapelEContagens() throws Exception {
        mvc.perform(como(ana, get("/api/v1/projetos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.itens[0].nome").value("Formação interna"))
                .andExpect(jsonPath("$.itens[0].meuPapel").value("MEMBRO"))
                .andExpect(jsonPath("$.itens[1].nome").value("Website institucional"))
                .andExpect(jsonPath("$.itens[1].meuPapel").value("GESTOR"))
                .andExpect(jsonPath("$.itens[1].gestor.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.itens[1].contagens.POR_FAZER").value(1))
                .andExpect(jsonPath("$.itens[1].contagens.EM_CURSO").value(1))
                .andExpect(jsonPath("$.itens[1].contagens.CONCLUIDA").value(1))
                .andExpect(jsonPath("$.itens[1].contagens.total").value(3));
    }

    @Test
    void pesquisaPorNomeSemMaiusculasNemAcentos() throws Exception {
        assertThat(nomes(ana, "web")).containsExactly("Website institucional");
        assertThat(nomes(ana, "FORMACAO")).containsExactly("Formação interna");
        assertThat(nomes(ana, "  interna ")).containsExactly("Formação interna");
        // "App móvel" existe, mas a Ana não é membro
        assertThat(nomes(ana, "app")).isEmpty();
        // Curingas do SQL são texto normal
        assertThat(nomes(ana, "%")).isEmpty();
        assertThat(nomes(ana, "_")).isEmpty();
    }

    @Test
    void pesquisaComMaisDe100CaracteresDa400() throws Exception {
        mvc.perform(como(ana, get("/api/v1/projetos").param("pesquisa", "a".repeat(101))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("pesquisa"));
    }

    // --- Criar ---------------------------------------------------------------------------------------------

    @Test
    void criarDeixaOUtilizadorComoGestorESoEleVe() throws Exception {
        JsonNode criado = lerJson(mvc.perform(como(rui, post("/api/v1/projetos"),
                        Map.of("nome", "  Website 2027  ", "descricao", "Novo site institucional.")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/projetos/" + idDe("Website 2027"))))
                .andExpect(jsonPath("$.nome").value("Website 2027"))
                .andExpect(jsonPath("$.meuPapel").value("GESTOR"))
                .andExpect(jsonPath("$.gestor.nome").value("Rui Costa"))
                .andExpect(jsonPath("$.contagens.total").value(0)));

        String id = criado.get("id").asText();
        mvc.perform(como(rui, get("/api/v1/projetos/" + id))).andExpect(status().isOk());
        mvc.perform(como(rui, get("/api/v1/projetos/" + id + "/membros")))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.itens[0].papel").value("GESTOR"));
        assertThat(nomes(rui, null)).contains("Website 2027");
        assertThat(nomes(ana, null)).doesNotContain("Website 2027");
        mvc.perform(como(ana, get("/api/v1/projetos/" + id))).andExpect(status().isNotFound());
    }

    @Test
    void oClienteNaoPodeIndicarOGestor() throws Exception {
        String corpo = "{\"nome\":\"Projeto X\",\"gestorId\":\"" + utilizador("ana@matriz.local") + "\"}";
        mvc.perform(como(rui, post("/api/v1/projetos"), corpo)).andExpect(status().isBadRequest());
    }

    @Test
    void validacaoDoNomeEDaDescricao() throws Exception {
        criarInvalido(Map.of("descricao", "sem nome"), "nome");
        criarInvalido(Map.of("nome", ""), "nome");
        criarInvalido(Map.of("nome", "   "), "nome");
        criarInvalido(Map.of("nome", "x".repeat(101)), "nome");
        criarInvalido(Map.of("nome", "Válido", "descricao", "d".repeat(1001)), "descricao");

        mvc.perform(como(ana, post("/api/v1/projetos"), Map.of("nome", "x".repeat(100), "descricao", "d".repeat(1000))))
                .andExpect(status().isCreated());
        mvc.perform(como(ana, post("/api/v1/projetos"), Map.of("nome", "A")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao").doesNotExist());
    }

    @Test
    void corpoQueNaoEJsonDa400() throws Exception {
        mvc.perform(como(ana, post("/api/v1/projetos"), "{nome:"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/erros/validacao"));
    }

    // --- Consultar -----------------------------------------------------------------------------------------

    @Test
    void projetoAlheioEInexistenteDaoOMesmo404() throws Exception {
        String alheio = mvc.perform(como(ana, get("/api/v1/projetos/" + idDe("App móvel"))))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();
        String inexistente = mvc.perform(como(ana, get("/api/v1/projetos/999999")))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();
        assertThat(semInstancia(alheio)).isEqualTo(semInstancia(inexistente));

        mvc.perform(como(ana, get("/api/v1/projetos/abc"))).andExpect(status().isNotFound());
        mvc.perform(como(ana, get("/api/v1/projetos/" + idDe("App móvel") + "/membros")))
                .andExpect(status().isNotFound());
    }

    @Test
    void semTokenDa401() throws Exception {
        mvc.perform(get("/api/v1/projetos")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/projetos")).andExpect(status().isUnauthorized());
    }

    // --- Editar --------------------------------------------------------------------------------------------

    @Test
    void soOGestorEdita() throws Exception {
        long website = idDe("Website institucional");
        mvc.perform(como(ana, patch("/api/v1/projetos/" + website), Map.of("nome", "Website novo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Website novo"));

        mvc.perform(como(rui, patch("/api/v1/projetos/" + website), Map.of("nome", "Do Rui")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Só o gestor do projeto pode fazer esta operação"));
        mvc.perform(como(joana, patch("/api/v1/projetos/" + website), Map.of("nome", "Da Joana")))
                .andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("SELECT nome FROM projeto WHERE id = ?", String.class, website))
                .isEqualTo("Website novo");
    }

    @Test
    void edicaoParcialEDescricaoANullApaga() throws Exception {
        long website = idDe("Website institucional");
        Map<String, Object> semDescricao = new HashMap<>();
        semDescricao.put("descricao", null);

        mvc.perform(como(ana, patch("/api/v1/projetos/" + website), semDescricao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Website institucional"))
                .andExpect(jsonPath("$.descricao").doesNotExist());
        mvc.perform(como(ana, patch("/api/v1/projetos/" + website), Map.of()))
                .andExpect(status().isBadRequest());
        mvc.perform(como(ana, patch("/api/v1/projetos/" + website), Map.of("nome", "  ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("nome"));
    }

    // --- Eliminar ------------------------------------------------------------------------------------------

    @Test
    void soOGestorEliminaESemDadosOrfaos() throws Exception {
        long website = idDe("Website institucional");
        mvc.perform(como(rui, delete("/api/v1/projetos/" + website))).andExpect(status().isForbidden());
        mvc.perform(como(joana, delete("/api/v1/projetos/" + website))).andExpect(status().isNotFound());

        long comentariosAntes = contar("comentario");
        mvc.perform(como(ana, delete("/api/v1/projetos/" + website))).andExpect(status().isNoContent());

        mvc.perform(como(ana, get("/api/v1/projetos/" + website))).andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM tarefa WHERE projeto_id = ?", Long.class, website)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM membro_projeto WHERE projeto_id = ?", Long.class, website))
                .isZero();
        assertThat(contar("comentario")).isEqualTo(comentariosAntes - 2);
        assertThat(nomes(rui, null)).containsExactly("App móvel");
    }

    // --- Membros -------------------------------------------------------------------------------------------

    @Test
    void membrosComOGestorPrimeiro() throws Exception {
        mvc.perform(como(rui, get("/api/v1/projetos/" + idDe("Website institucional") + "/membros")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.itens[0].utilizador.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.itens[0].papel").value("GESTOR"))
                .andExpect(jsonPath("$.itens[1].utilizador.email").value("rui@matriz.local"))
                .andExpect(jsonPath("$.itens[1].papel").value("MEMBRO"));
    }

    @Test
    void gestorAdicionaMembroPorEmail() throws Exception {
        long website = idDe("Website institucional");
        mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/membros"), Map.of("email", "JOANA@matriz.local")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.utilizador.nome").value("Joana Lopes"))
                .andExpect(jsonPath("$.papel").value("MEMBRO"));
        assertThat(nomes(joana, "website")).containsExactly("Website institucional");

        mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/membros"), Map.of("email", "joana@matriz.local")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Este utilizador já é membro do projeto"));
        mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/membros"), Map.of("email", "ninguem@matriz.local")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Não existe utilizador com este email"));
        mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/membros"), Map.of("email", "nao-e-email")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void soOGestorGereMembros() throws Exception {
        long website = idDe("Website institucional");
        mvc.perform(como(rui, post("/api/v1/projetos/" + website + "/membros"), Map.of("email", "joana@matriz.local")))
                .andExpect(status().isForbidden());
        mvc.perform(como(rui, delete("/api/v1/projetos/" + website + "/membros/" + utilizador("ana@matriz.local"))))
                .andExpect(status().isForbidden());
        mvc.perform(como(joana, post("/api/v1/projetos/" + website + "/membros"), Map.of("email", "joana@matriz.local")))
                .andExpect(status().isNotFound());
    }

    @Test
    void oGestorNaoPodeSerRemovido() throws Exception {
        long website = idDe("Website institucional");
        mvc.perform(como(ana, delete("/api/v1/projetos/" + website + "/membros/" + utilizador("ana@matriz.local"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("O gestor não pode ser removido do projeto"));
    }

    @Test
    void removerMembroTiraAcessoEAtribuicoesMasMantemAsTarefas() throws Exception {
        long website = idDe("Website institucional");
        long idRui = utilizador("rui@matriz.local");
        long tarefasAntes = jdbc.queryForObject("SELECT count(*) FROM tarefa WHERE projeto_id = ?", Long.class, website);

        mvc.perform(como(ana, delete("/api/v1/projetos/" + website + "/membros/" + idRui)))
                .andExpect(status().isNoContent());

        mvc.perform(como(rui, get("/api/v1/projetos/" + website))).andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM tarefa_responsavel r JOIN tarefa t ON t.id = r.tarefa_id
                WHERE t.projeto_id = ? AND r.utilizador_id = ?""", Long.class, website, idRui)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM tarefa WHERE projeto_id = ?", Long.class, website))
                .isEqualTo(tarefasAntes);
        // O Rui continua membro do projeto de que é Gestor
        assertThat(jdbc.queryForObject("SELECT count(*) FROM membro_projeto WHERE utilizador_id = ?", Long.class, idRui))
                .isEqualTo(1);

        mvc.perform(como(ana, delete("/api/v1/projetos/" + website + "/membros/" + idRui)))
                .andExpect(status().isNotFound());
    }

    // --- Auxiliares ----------------------------------------------------------------------------------------

    private List<String> nomes(String token, String pesquisa) throws Exception {
        var pedido = get("/api/v1/projetos");
        if (pesquisa != null) {
            pedido.param("pesquisa", pesquisa);
        }
        JsonNode lista = lerJson(mvc.perform(como(token, pedido)).andExpect(status().isOk()));
        List<String> nomes = new ArrayList<>();
        lista.get("itens").forEach(item -> nomes.add(item.get("nome").asText()));
        return nomes;
    }

    private void criarInvalido(Map<String, Object> corpo, String campo) throws Exception {
        mvc.perform(como(ana, post("/api/v1/projetos"), corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/erros/validacao"))
                .andExpect(jsonPath("$.erros[0].campo").value(campo));
    }

    private long idDe(String nome) {
        return jdbc.queryForObject("SELECT id FROM projeto WHERE nome = ?", Long.class, nome);
    }

    private long utilizador(String email) {
        return jdbc.queryForObject("SELECT id FROM utilizador WHERE email = ?", Long.class, email);
    }

    private long contar(String tabela) {
        return jdbc.queryForObject("SELECT count(*) FROM " + tabela, Long.class);
    }

    private static String semInstancia(String problema) {
        return problema.replaceAll(",?\"instance\":\"[^\"]*\"", "");
    }
}
