package pt.matriz.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
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
 * Ficha 03, Ex. 5: tarefas por HTTP, sobre os dados do seed. "Website institucional": Gestora Ana, membro Rui,
 * três tarefas; Joana não tem acesso. Cada teste corre numa transação desfeita no fim.
 */
@Transactional
class TarefasTest extends ApiTeste {

    private static final String MAQUETAS = "Criar as maquetas da página inicial";
    private static final String TEXTOS = "Escrever os textos da secção Sobre";

    @Autowired
    JdbcTemplate jdbc;

    String ana;
    String rui;
    String joana;
    long website;

    @BeforeEach
    void entrar() throws Exception {
        ana = token("ana@matriz.local");
        rui = token("rui@matriz.local");
        joana = token("joana@matriz.local");
        website = jdbc.queryForObject("SELECT id FROM projeto WHERE nome = 'Website institucional'", Long.class);
    }

    // --- Listar e filtrar ---------------------------------------------------------------------------------

    @Test
    void listaAsTarefasDoProjetoComResponsaveisComentariosEAtrasada() throws Exception {
        JsonNode lista = lerJson(mvc.perform(como(rui, get("/api/v1/projetos/" + website + "/tarefas")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3)));
        JsonNode maquetas = tarefa(lista, MAQUETAS);
        assertThat(maquetas.get("estado").asText()).isEqualTo("EM_CURSO");
        assertThat(maquetas.get("prioridade").asText()).isEqualTo("ALTA");
        assertThat(maquetas.get("projetoId").asText()).isEqualTo(String.valueOf(website));
        assertThat(maquetas.at("/criador/nome").asText()).isEqualTo("Ana Silva");
        assertThat(maquetas.get("responsaveis").findValuesAsText("nome")).containsExactly("Ana Silva", "Rui Costa");
        assertThat(maquetas.get("totalComentarios").asLong()).isEqualTo(2);
        // Data limite há 2 dias e não concluída
        assertThat(maquetas.get("atrasada").asBoolean()).isTrue();
        // Data limite passada, mas concluída
        assertThat(tarefa(lista, "Definir o mapa do site").get("atrasada").asBoolean()).isFalse();
        assertThat(tarefa(lista, TEXTOS).get("atrasada").asBoolean()).isFalse();
    }

    @Test
    void filtrosPorEstadoPrioridadeEResponsavelCombinam() throws Exception {
        long ruiId = utilizador("rui@matriz.local");
        assertThat(titulos("?estado=POR_FAZER")).containsExactly(TEXTOS);
        assertThat(titulos("?prioridade=ALTA")).containsExactlyInAnyOrder(MAQUETAS, "Definir o mapa do site");
        assertThat(titulos("?responsavelId=" + ruiId)).containsExactly(MAQUETAS);
        assertThat(titulos("?prioridade=ALTA&estado=CONCLUIDA")).containsExactly("Definir o mapa do site");
        assertThat(titulos("?prioridade=ALTA&responsavelId=" + ruiId + "&estado=POR_FAZER")).isEmpty();
    }

    @Test
    void filtroComValorInvalidoDa400() throws Exception {
        mvc.perform(como(ana, get("/api/v1/projetos/" + website + "/tarefas?estado=FEITA")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("estado"));
        mvc.perform(como(ana, get("/api/v1/projetos/" + website + "/tarefas?responsavelId=abc")))
                .andExpect(status().isBadRequest());
    }

    // --- Acesso -------------------------------------------------------------------------------------------

    @Test
    void quemNaoEMembroRecebe404EmTodasAsOperacoes() throws Exception {
        long maquetas = idDe(MAQUETAS);
        mvc.perform(como(joana, get("/api/v1/projetos/" + website + "/tarefas"))).andExpect(status().isNotFound());
        mvc.perform(como(joana, post("/api/v1/projetos/" + website + "/tarefas"), Map.of("titulo", "Intrusa")))
                .andExpect(status().isNotFound());
        mvc.perform(como(joana, get("/api/v1/tarefas/" + maquetas))).andExpect(status().isNotFound());
        mvc.perform(como(joana, patch("/api/v1/tarefas/" + maquetas), Map.of("estado", "CONCLUIDA")))
                .andExpect(status().isNotFound());
        mvc.perform(como(joana, delete("/api/v1/tarefas/" + maquetas))).andExpect(status().isNotFound());
        // Igual a uma tarefa inexistente
        mvc.perform(como(joana, get("/api/v1/tarefas/999999"))).andExpect(status().isNotFound());
        assertThat(contar("tarefa WHERE titulo = 'Intrusa'")).isZero();
        assertThat(jdbc.queryForObject("SELECT estado FROM tarefa WHERE id = ?", String.class, maquetas))
                .isEqualTo("EM_CURSO");
    }

    // --- Criar --------------------------------------------------------------------------------------------

    @Test
    void membroCriaTarefaQueFicaPorFazerComEleComoCriador() throws Exception {
        long anaId = utilizador("ana@matriz.local");
        JsonNode criada = lerJson(mvc.perform(como(rui, post("/api/v1/projetos/" + website + "/tarefas"),
                        Map.of("titulo", "  Rever o logótipo  ", "responsavelIds", List.of(String.valueOf(anaId)),
                                "dataLimite", "2030-01-15")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/tarefas/")))
                .andExpect(jsonPath("$.titulo").value("Rever o logótipo"))
                .andExpect(jsonPath("$.estado").value("POR_FAZER"))
                .andExpect(jsonPath("$.prioridade").value("MEDIA"))
                .andExpect(jsonPath("$.criador.nome").value("Rui Costa"))
                .andExpect(jsonPath("$.responsaveis[0].nome").value("Ana Silva"))
                .andExpect(jsonPath("$.dataLimite").value("2030-01-15"))
                .andExpect(jsonPath("$.atrasada").value(false))
                .andExpect(jsonPath("$.totalComentarios").value(0)));

        String id = criada.get("id").asText();
        assertThat(criada.get("projetoId").asText()).isEqualTo(String.valueOf(website));
        mvc.perform(como(ana, get("/api/v1/projetos/" + website + "/tarefas")))
                .andExpect(jsonPath("$.itens[0].id").value(id));

        // Fica guardada e as contagens do projeto refletem-na
        mvc.perform(como(ana, get("/api/v1/tarefas/" + criada.get("id").asText())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Rever o logótipo"));
        mvc.perform(como(ana, get("/api/v1/projetos/" + website)))
                .andExpect(jsonPath("$.contagens.POR_FAZER").value(2))
                .andExpect(jsonPath("$.contagens.total").value(4));
    }

    @Test
    void validacaoDoTituloEDaDescricao() throws Exception {
        criarInvalido(Map.of(), "titulo");
        criarInvalido(Map.of("titulo", "   "), "titulo");
        criarInvalido(Map.of("titulo", "x".repeat(201)), "titulo");
        criarInvalido(Map.of("titulo", "Válido", "descricao", "x".repeat(2001)), "descricao");
        criarInvalido(Map.of("titulo", "Válido", "prioridade", "URGENTE"), "prioridade");
        criarInvalido(Map.of("titulo", "Válido", "dataLimite", "31/12/2030"), "dataLimite");
        // 200 caracteres é o limite
        mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/tarefas"), Map.of("titulo", "x".repeat(200))))
                .andExpect(status().isCreated());
    }

    @Test
    void oClienteNaoPodeIndicarOCriadorNemOEstado() throws Exception {
        criarInvalido(Map.of("titulo", "Válido", "criadorId", "1"), "criadorId");
        criarInvalido(Map.of("titulo", "Válido", "estado", "CONCLUIDA"), "estado");
    }

    @Test
    void responsaveisTemDeSerMembrosSemRepeticoes() throws Exception {
        String anaId = String.valueOf(utilizador("ana@matriz.local"));
        String joanaId = String.valueOf(utilizador("joana@matriz.local"));
        mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/tarefas"),
                        Map.of("titulo", "Válido", "responsavelIds", List.of(joanaId))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("/erros/responsavel-nao-membro"));
        criarInvalido(Map.of("titulo", "Válido", "responsavelIds", List.of(anaId, anaId)), "responsavelIds");
        criarInvalido(Map.of("titulo", "Válido", "responsavelIds", List.of("abc")), "responsavelIds");
        assertThat(contar("tarefa WHERE titulo = 'Válido'")).isZero();
    }

    // --- Editar -------------------------------------------------------------------------------------------

    @Test
    void qualquerMembroMudaEstadoEPrioridadeEAsContagensAcompanham() throws Exception {
        long maquetas = idDe(MAQUETAS);
        mvc.perform(como(rui, patch("/api/v1/tarefas/" + maquetas), Map.of("estado", "CONCLUIDA", "prioridade", "BAIXA")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONCLUIDA"))
                .andExpect(jsonPath("$.prioridade").value("BAIXA"))
                .andExpect(jsonPath("$.atrasada").value(false))
                .andExpect(jsonPath("$.titulo").value(MAQUETAS));
        mvc.perform(como(ana, get("/api/v1/projetos/" + website)))
                .andExpect(jsonPath("$.contagens.EM_CURSO").value(0))
                .andExpect(jsonPath("$.contagens.CONCLUIDA").value(2));
        // Qualquer transição é permitida (P-04)
        mvc.perform(como(rui, patch("/api/v1/tarefas/" + maquetas), Map.of("estado", "POR_FAZER")))
                .andExpect(status().isOk());
    }

    @Test
    void edicaoParcialEValoresANullApagam() throws Exception {
        long maquetas = idDe(MAQUETAS);
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("descricao", null);
        corpo.put("dataLimite", null);
        mvc.perform(como(ana, patch("/api/v1/tarefas/" + maquetas), corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").doesNotExist())
                .andExpect(jsonPath("$.dataLimite").doesNotExist())
                .andExpect(jsonPath("$.atrasada").value(false))
                .andExpect(jsonPath("$.estado").value("EM_CURSO"))
                .andExpect(jsonPath("$.responsaveis.length()").value(2));
    }

    @Test
    void edicaoInvalidaDa400ENaoAltera() throws Exception {
        long maquetas = idDe(MAQUETAS);
        editarInvalido(maquetas, Map.of(), "titulo");
        editarInvalido(maquetas, Map.of("titulo", "  "), "titulo");
        editarInvalido(maquetas, Map.of("estado", "FEITA"), "estado");
        Map<String, Object> estadoNulo = new HashMap<>();
        estadoNulo.put("estado", null);
        editarInvalido(maquetas, estadoNulo, "estado");
        editarInvalido(maquetas, Map.of("responsavelIds", List.of()), "responsavelIds");
        assertThat(jdbc.queryForObject("SELECT titulo || estado FROM tarefa WHERE id = ?", String.class, maquetas))
                .isEqualTo(MAQUETAS + "EM_CURSO");
    }

    // --- Eliminar -----------------------------------------------------------------------------------------

    @Test
    void soOCriadorOuOGestorEliminam() throws Exception {
        long maquetas = idDe(MAQUETAS); // criada pela Ana (Gestora)
        long textos = idDe(TEXTOS); // criada pelo Rui (membro)

        mvc.perform(como(rui, delete("/api/v1/tarefas/" + maquetas)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Só quem criou a tarefa ou o gestor do projeto a pode eliminar"));
        mvc.perform(como(rui, delete("/api/v1/tarefas/" + textos))).andExpect(status().isNoContent());
        mvc.perform(como(rui, get("/api/v1/tarefas/" + textos))).andExpect(status().isNotFound());

        // A Gestora elimina uma tarefa criada por outro membro
        long doRui = lerJson(mvc.perform(como(rui, post("/api/v1/projetos/" + website + "/tarefas"),
                Map.of("titulo", "Do Rui")))).get("id").asLong();
        mvc.perform(como(ana, delete("/api/v1/tarefas/" + doRui))).andExpect(status().isNoContent());
    }

    @Test
    void eliminarTiraAtribuicoesEComentarios() throws Exception {
        long maquetas = idDe(MAQUETAS);
        mvc.perform(como(ana, delete("/api/v1/tarefas/" + maquetas))).andExpect(status().isNoContent());
        assertThat(contar("tarefa_responsavel WHERE tarefa_id = " + maquetas)).isZero();
        assertThat(contar("comentario WHERE tarefa_id = " + maquetas)).isZero();
        mvc.perform(como(ana, get("/api/v1/projetos/" + website)))
                .andExpect(jsonPath("$.contagens.total").value(2));
    }

    // --- Auxiliares ---------------------------------------------------------------------------------------

    private List<String> titulos(String filtro) throws Exception {
        JsonNode lista = lerJson(mvc.perform(como(ana, get("/api/v1/projetos/" + website + "/tarefas" + filtro)))
                .andExpect(status().isOk()));
        List<String> titulos = new ArrayList<>();
        lista.get("itens").forEach(item -> titulos.add(item.get("titulo").asText()));
        assertThat(lista.get("total").asInt()).isEqualTo(titulos.size());
        return titulos;
    }

    private static JsonNode tarefa(JsonNode lista, String titulo) {
        for (JsonNode item : lista.get("itens")) {
            if (item.get("titulo").asText().equals(titulo)) {
                return item;
            }
        }
        throw new AssertionError("Tarefa não encontrada: " + titulo);
    }

    private void criarInvalido(Map<String, Object> corpo, String campo) throws Exception {
        mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/tarefas"), corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/erros/validacao"))
                .andExpect(jsonPath("$.erros[0].campo").value(campo));
    }

    private void editarInvalido(long tarefa, Map<String, Object> corpo, String campo) throws Exception {
        mvc.perform(como(ana, patch("/api/v1/tarefas/" + tarefa), corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value(campo));
    }

    private long idDe(String titulo) {
        return jdbc.queryForObject("SELECT id FROM tarefa WHERE titulo = ?", Long.class, titulo);
    }

    private long utilizador(String email) {
        return jdbc.queryForObject("SELECT id FROM utilizador WHERE email = ?", Long.class, email);
    }

    private long contar(String tabelaECondicao) {
        return jdbc.queryForObject("SELECT count(*) FROM " + tabelaECondicao, Long.class);
    }
}
