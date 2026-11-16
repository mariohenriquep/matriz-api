package pt.matriz.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import pt.matriz.ApiTeste;

/** Quadro geral da página inicial: {@code GET /tarefas} com as tarefas de todos os projetos ativos do utilizador. */
@Transactional
class QuadroGeralTest extends ApiTeste {

    @Autowired
    JdbcTemplate jdbc;

    String ana;
    String rui;
    String joana;
    long anaId;
    long website;
    long app;
    long formacao;

    @BeforeEach
    void entrar() throws Exception {
        ana = token("ana@matriz.local");
        rui = token("rui@matriz.local");
        joana = token("joana@matriz.local");
        anaId = jdbc.queryForObject("SELECT id FROM utilizador WHERE email = 'ana@matriz.local'", Long.class);
        website = projeto("Website institucional");
        app = projeto("App móvel");
        formacao = projeto("Formação interna");
    }

    @Test
    void cadaUtilizadorVeAsTarefasDosSeusProjetos() throws Exception {
        assertThat(titulos(ana, "")).containsExactlyInAnyOrder("Definir o mapa do site",
                "Criar as maquetas da página inicial", "Escrever os textos da secção Sobre",
                "Preparar a sessão sobre Git");
        assertThat(titulos(rui, "")).hasSize(5);
        assertThat(titulos(joana, "")).hasSize(3);

        mvc.perform(como(ana, get("/api/v1/tarefas")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(4))
                .andExpect(jsonPath("$.itens[0].projetoId").isNotEmpty());
    }

    @Test
    void osProjetosArquivadosFicamDeFora() throws Exception {
        mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/arquivar"))).andExpect(status().isOk());

        assertThat(titulos(ana, "")).containsExactly("Preparar a sessão sobre Git");
        assertThat(titulos(rui, "")).hasSize(2);
        assertThat(titulos(ana, "?projetoId=" + website)).isEmpty();
    }

    @Test
    void osFiltrosCombinamComOsProjetos() throws Exception {
        assertThat(titulos(ana, "?projetoId=" + formacao)).containsExactly("Preparar a sessão sobre Git");
        assertThat(titulos(ana, "?prioridade=ALTA")).hasSize(3);
        assertThat(titulos(ana, "?estado=POR_FAZER")).containsExactlyInAnyOrder(
                "Escrever os textos da secção Sobre", "Preparar a sessão sobre Git");
        assertThat(titulos(ana, "?responsavelId=" + anaId)).containsExactlyInAnyOrder("Definir o mapa do site",
                "Criar as maquetas da página inicial", "Preparar a sessão sobre Git");
        assertThat(titulos(ana, "?responsavelId=" + anaId + "&prioridade=ALTA&projetoId=" + website))
                .containsExactlyInAnyOrder("Definir o mapa do site", "Criar as maquetas da página inicial");
    }

    @Test
    void umProjetoDeQueNaoSeEMembroNaoMostraNada() throws Exception {
        assertThat(titulos(ana, "?projetoId=" + app)).isEmpty();
        assertThat(titulos(ana, "?projetoId=999999")).isEmpty();
    }

    @Test
    void valoresInvalidosESemSessao() throws Exception {
        mvc.perform(como(ana, get("/api/v1/tarefas?estado=INVENTADO"))).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/tarefas")).andExpect(status().isUnauthorized());
    }

    private long projeto(String nome) {
        return jdbc.queryForObject("SELECT id FROM projeto WHERE nome = ?", Long.class, nome);
    }

    private List<String> titulos(String token, String consulta) throws Exception {
        JsonNode corpo = lerJson(mvc.perform(como(token, get("/api/v1/tarefas" + consulta))).andExpect(status().isOk()));
        List<String> titulos = new ArrayList<>();
        corpo.get("itens").forEach(item -> titulos.add(item.get("titulo").asText()));
        return titulos;
    }
}
