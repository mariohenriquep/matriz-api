package pt.matriz.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import pt.matriz.ApiTeste;

/** Arquivo de projetos (extra do MVP): arquivar, reativar, listagens e projeto arquivado só de leitura. */
@Transactional
class ArquivoProjetosTest extends ApiTeste {

    @Autowired
    JdbcTemplate jdbc;

    String ana;
    String rui;
    String joana;
    long website;
    long tarefa;

    @BeforeEach
    void entrar() throws Exception {
        ana = token("ana@matriz.local");
        rui = token("rui@matriz.local");
        joana = token("joana@matriz.local");
        website = jdbc.queryForObject("SELECT id FROM projeto WHERE nome = 'Website institucional'", Long.class);
        tarefa = jdbc.queryForObject("SELECT min(id) FROM tarefa WHERE projeto_id = ?", Long.class, website);
    }

    @Test
    void oGestorArquivaEReativaEARespostaIndicaOEstado() throws Exception {
        mvc.perform(como(ana, get("/api/v1/projetos/" + website)))
                .andExpect(jsonPath("$.arquivado").value(false))
                .andExpect(jsonPath("$.arquivadoEm").doesNotExist());

        mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/arquivar")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.arquivado").value(true))
                .andExpect(jsonPath("$.arquivadoEm").isNotEmpty())
                .andExpect(jsonPath("$.contagens.total").value(3));

        mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/reativar")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.arquivado").value(false))
                .andExpect(jsonPath("$.arquivadoEm").doesNotExist());
    }

    @Test
    void arquivarDuasVezesMantemADataDoPrimeiroArquivo() throws Exception {
        String primeira = lerJson(mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/arquivar"))))
                .get("arquivadoEm").asText();
        mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/arquivar")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.arquivadoEm").value(primeira));
        // Reativar um projeto ativo também não é erro
        mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/reativar")))
                .andExpect(status().isOk());
        mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/reativar")))
                .andExpect(status().isOk());
    }

    @Test
    void soOGestorArquivaOuReativaEQuemNaoEMembroRecebe404() throws Exception {
        mvc.perform(como(rui, post("/api/v1/projetos/" + website + "/arquivar"))).andExpect(status().isForbidden());
        mvc.perform(como(joana, post("/api/v1/projetos/" + website + "/arquivar"))).andExpect(status().isNotFound());
        mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/arquivar"))).andExpect(status().isOk());
        mvc.perform(como(rui, post("/api/v1/projetos/" + website + "/reativar"))).andExpect(status().isForbidden());
        mvc.perform(como(joana, post("/api/v1/projetos/" + website + "/reativar"))).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/projetos/" + website + "/arquivar")).andExpect(status().isUnauthorized());
    }

    @Test
    void aListaPrincipalSoTemAtivosEOsArquivadosTemListaPropria() throws Exception {
        mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/arquivar"))).andExpect(status().isOk());

        assertThat(nomes(ana, "")).containsExactly("Formação interna");
        assertThat(nomes(ana, "?arquivados=false")).containsExactly("Formação interna");
        assertThat(nomes(ana, "?arquivados=true")).containsExactly("Website institucional");
        // A pesquisa funciona nas duas listas; o Rui (membro) também vê o projeto no arquivo
        assertThat(nomes(rui, "?arquivados=true&pesquisa=WEB")).containsExactly("Website institucional");
        assertThat(nomes(ana, "?arquivados=true&pesquisa=forma")).isEmpty();
        assertThat(nomes(joana, "?arquivados=true")).isEmpty();
    }

    @Test
    void umProjetoArquivadoContinuaConsultavel() throws Exception {
        mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/arquivar"))).andExpect(status().isOk());

        mvc.perform(como(rui, get("/api/v1/projetos/" + website))).andExpect(status().isOk());
        mvc.perform(como(rui, get("/api/v1/projetos/" + website + "/membros"))).andExpect(status().isOk());
        mvc.perform(como(rui, get("/api/v1/projetos/" + website + "/tarefas")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3));
        mvc.perform(como(rui, get("/api/v1/tarefas/" + tarefa))).andExpect(status().isOk());
        mvc.perform(como(rui, get("/api/v1/tarefas/" + tarefa + "/comentarios"))).andExpect(status().isOk());
    }

    @Test
    void umProjetoArquivadoNaoAceitaAlteracoes() throws Exception {
        long rui = jdbc.queryForObject("SELECT id FROM utilizador WHERE email = 'rui@matriz.local'", Long.class);
        mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/arquivar"))).andExpect(status().isOk());

        List<MockHttpServletRequestBuilder> pedidos = new ArrayList<>(List.of(
                como(ana, patch("/api/v1/projetos/" + website), Map.of("nome", "Outro nome")),
                como(ana, post("/api/v1/projetos/" + website + "/membros"), Map.of("email", "joana@matriz.local")),
                como(ana, delete("/api/v1/projetos/" + website + "/membros/" + rui)),
                como(ana, post("/api/v1/projetos/" + website + "/tarefas"), Map.of("titulo", "Nova")),
                como(ana, patch("/api/v1/tarefas/" + tarefa), Map.of("estado", "CONCLUIDA")),
                como(ana, post("/api/v1/tarefas/" + tarefa + "/responsaveis"), Map.of("utilizadorId", String.valueOf(rui))),
                como(ana, delete("/api/v1/tarefas/" + tarefa + "/responsaveis/" + rui)),
                como(ana, post("/api/v1/tarefas/" + tarefa + "/comentarios"), Map.of("conteudo", "Olá")),
                como(ana, delete("/api/v1/tarefas/" + tarefa))));
        long tarefasAntes = contar("tarefa");
        long comentariosAntes = contar("comentario");
        for (MockHttpServletRequestBuilder pedido : pedidos) {
            mvc.perform(pedido)
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.type").value(org.hamcrest.Matchers.endsWith("/erros/projeto-arquivado")));
        }
        assertThat(contar("tarefa")).isEqualTo(tarefasAntes);
        assertThat(contar("comentario")).isEqualTo(comentariosAntes);
        assertThat(jdbc.queryForObject("SELECT nome FROM projeto WHERE id = ?", String.class, website))
                .isEqualTo("Website institucional");
    }

    @Test
    void quemNaoEMembroContinuaARecebe404MesmoComOProjetoArquivado() throws Exception {
        mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/arquivar"))).andExpect(status().isOk());
        mvc.perform(como(joana, patch("/api/v1/tarefas/" + tarefa), Map.of("estado", "CONCLUIDA")))
                .andExpect(status().isNotFound());
        mvc.perform(como(joana, post("/api/v1/projetos/" + website + "/tarefas"), Map.of("titulo", "Nova")))
                .andExpect(status().isNotFound());
    }

    @Test
    void depoisDeReativarVoltaAAceitarAlteracoes() throws Exception {
        mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/arquivar"))).andExpect(status().isOk());
        mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/reativar"))).andExpect(status().isOk());
        mvc.perform(como(rui, patch("/api/v1/tarefas/" + tarefa), Map.of("estado", "CONCLUIDA")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONCLUIDA"));
    }

    @Test
    void oGestorPodeEliminarUmProjetoArquivado() throws Exception {
        mvc.perform(como(ana, post("/api/v1/projetos/" + website + "/arquivar"))).andExpect(status().isOk());
        mvc.perform(como(ana, delete("/api/v1/projetos/" + website))).andExpect(status().isNoContent());
        mvc.perform(como(ana, get("/api/v1/projetos/" + website))).andExpect(status().isNotFound());
    }

    private List<String> nomes(String token, String consulta) throws Exception {
        JsonNode lista = lerJson(mvc.perform(como(token, get("/api/v1/projetos" + consulta))).andExpect(status().isOk()));
        List<String> nomes = new ArrayList<>();
        lista.get("itens").forEach(p -> nomes.add(p.get("nome").asText()));
        return nomes;
    }

    private long contar(String tabela) {
        return jdbc.queryForObject("SELECT count(*) FROM " + tabela, Long.class);
    }
}
