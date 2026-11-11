package pt.matriz.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.ResultActions;
import pt.matriz.ApiTeste;

/**
 * Teste de integração da Ficha 04, Ex. 3 (casos T-01, T-05 e T-09 de docs/test-plan.md): criar uma tarefa e
 * consultá-la pela API, com a aplicação completa e um PostgreSQL 16 real (Testcontainers).
 *
 * <p>Ao contrário dos outros testes da API, esta classe <b>não</b> usa {@code @Transactional}: cada pedido faz
 * commit a sério, como em produção, e a persistência é confirmada por uma consulta SQL independente (outra
 * ligação, fora de qualquer transação do teste). Para não depender nem alterar os dados de outros testes, cada
 * teste cria o seu próprio projeto, com nome único, e elimina-o no fim (a eliminação em cascata leva as
 * tarefas e os membros). O {@link #limpar()} confirma que nada ficou para trás.
 */
class CriarEConsultarTarefaTest extends ApiTeste {

    @Autowired
    JdbcTemplate jdbc;

    String ana;
    String rui;
    String joana;
    long projeto;

    @BeforeEach
    void prepararProjetoIsolado() throws Exception {
        ana = token("ana@matriz.local");
        rui = token("rui@matriz.local");
        joana = token("joana@matriz.local");

        // Projeto próprio do teste: a Ana é Gestora e junta o Rui como membro. A Joana fica de fora.
        JsonNode criado = lerJson(mvc.perform(como(ana, post("/api/v1/projetos"),
                        Map.of("nome", "Ex3 integração " + UUID.randomUUID())))
                .andExpect(status().isCreated()));
        projeto = criado.get("id").asLong();
        mvc.perform(como(ana, post("/api/v1/projetos/" + projeto + "/membros"), Map.of("email", "rui@matriz.local")))
                .andExpect(status().isCreated());
    }

    @AfterEach
    void limpar() {
        jdbc.update("DELETE FROM projeto WHERE id = ?", projeto);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM tarefa WHERE projeto_id = ?", Long.class, projeto))
                .isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM membro_projeto WHERE projeto_id = ?", Long.class,
                projeto)).isZero();
    }

    @Test
    void membroCriaUmaTarefaQueFicaGravadaEPodeSerConsultada() throws Exception {
        long ruiId = jdbc.queryForObject("SELECT id FROM utilizador WHERE email = 'rui@matriz.local'", Long.class);

        // 1. Criar (US-06 CA1 e CA2): o servidor define o estado, o criador e as datas
        ResultActions resposta = mvc.perform(como(rui, post("/api/v1/projetos/" + projeto + "/tarefas"),
                        Map.of("titulo", "Rever logótipo", "descricao", "Versão a cores e a preto",
                                "prioridade", "ALTA", "dataLimite", "2030-11-15")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Rever logótipo"))
                .andExpect(jsonPath("$.descricao").value("Versão a cores e a preto"))
                .andExpect(jsonPath("$.estado").value("POR_FAZER"))
                .andExpect(jsonPath("$.prioridade").value("ALTA"))
                .andExpect(jsonPath("$.dataLimite").value("2030-11-15"))
                .andExpect(jsonPath("$.criador.nome").value("Rui Costa"))
                .andExpect(jsonPath("$.responsaveis").isEmpty())
                .andExpect(jsonPath("$.atrasada").value(false))
                .andExpect(jsonPath("$.criadoEm").isNotEmpty());
        JsonNode criada = lerJson(resposta);
        String id = criada.get("id").asText();
        assertThat(resposta.andReturn().getResponse().getHeader("Location")).endsWith("/api/v1/tarefas/" + id);
        assertThat(criada.get("projetoId").asText()).isEqualTo(String.valueOf(projeto));

        // 2. Persistência real: a linha está na base de dados, lida por uma ligação independente
        Map<String, Object> linha = jdbc.queryForMap(
                "SELECT projeto_id, titulo, descricao, estado, prioridade, criador_id, data_limite::text AS data_limite"
                        + " FROM tarefa WHERE id = ?", Long.parseLong(id));
        assertThat(linha)
                .containsEntry("projeto_id", projeto)
                .containsEntry("titulo", "Rever logótipo")
                .containsEntry("descricao", "Versão a cores e a preto")
                .containsEntry("estado", "POR_FAZER")
                .containsEntry("prioridade", "ALTA")
                .containsEntry("criador_id", ruiId)
                .containsEntry("data_limite", "2030-11-15");

        // 3. Consultar pelo URL do Location e na lista do projeto, como outro membro (a Gestora)
        mvc.perform(como(ana, get("/api/v1/tarefas/" + id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.titulo").value("Rever logótipo"))
                .andExpect(jsonPath("$.prioridade").value("ALTA"))
                .andExpect(jsonPath("$.criador.nome").value("Rui Costa"));
        mvc.perform(como(ana, get("/api/v1/projetos/" + projeto + "/tarefas")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.itens[0].id").value(id));
        mvc.perform(como(ana, get("/api/v1/projetos/" + projeto)))
                .andExpect(jsonPath("$.contagens.POR_FAZER").value(1))
                .andExpect(jsonPath("$.contagens.total").value(1));
    }

    @Test
    void tituloInvalidoDa400ENadaFicaGravado() throws Exception {
        for (Object titulo : new Object[] {"", "   ", "x".repeat(201)}) {
            mvc.perform(como(rui, post("/api/v1/projetos/" + projeto + "/tarefas"), Map.of("titulo", titulo)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type").value("/erros/validacao"))
                    .andExpect(jsonPath("$.erros[0].campo").value("titulo"));
        }
        // Sem título nenhum
        mvc.perform(como(rui, post("/api/v1/projetos/" + projeto + "/tarefas"), Map.of("descricao", "Sem título")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("titulo"));

        assertThat(jdbc.queryForObject("SELECT count(*) FROM tarefa WHERE projeto_id = ?", Long.class, projeto))
                .isZero();
    }

    @Test
    void tarefaInexistenteEAlheiaDaoOMesmo404() throws Exception {
        String id = lerJson(mvc.perform(como(rui, post("/api/v1/projetos/" + projeto + "/tarefas"),
                Map.of("titulo", "Só para membros")))).get("id").asText();
        long inexistente = jdbc.queryForObject("SELECT coalesce(max(id), 0) + 1000 FROM tarefa", Long.class);

        JsonNode naoExiste = lerJson(mvc.perform(como(ana, get("/api/v1/tarefas/" + inexistente)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("/erros/nao-encontrado")));
        // A Joana não é membro: recebe a mesma resposta que para uma tarefa que não existe (CE-07)
        JsonNode alheia = lerJson(mvc.perform(como(joana, get("/api/v1/tarefas/" + id)))
                .andExpect(status().isNotFound()));
        assertThat(alheia.get("detail")).isEqualTo(naoExiste.get("detail"));
        assertThat(alheia.get("type")).isEqualTo(naoExiste.get("type"));
    }
}
