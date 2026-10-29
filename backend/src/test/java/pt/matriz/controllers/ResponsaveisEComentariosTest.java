package pt.matriz.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import pt.matriz.ApiTeste;

/**
 * Ficha 03, Ex. 6: responsáveis e comentários por HTTP, sobre o seed. "Website institucional": Gestora Ana,
 * membro Rui; Joana não tem acesso. "Escrever os textos" não tem responsáveis; "Criar as maquetas" tem a Ana
 * e o Rui e 2 comentários.
 */
@Transactional
class ResponsaveisEComentariosTest extends ApiTeste {

    @Autowired
    JdbcTemplate jdbc;

    String ana;
    String rui;
    String joana;
    long textos;
    long maquetas;

    @BeforeEach
    void entrar() throws Exception {
        ana = token("ana@matriz.local");
        rui = token("rui@matriz.local");
        joana = token("joana@matriz.local");
        textos = idDe("Escrever os textos da secção Sobre");
        maquetas = idDe("Criar as maquetas da página inicial");
    }

    // --- Responsáveis -------------------------------------------------------------------------------------

    @Test
    void qualquerMembroAtribuiVariosResponsaveis() throws Exception {
        mvc.perform(como(rui, post("/api/v1/tarefas/" + textos + "/responsaveis"), atribuicao("ana@matriz.local")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responsaveis.length()").value(1))
                .andExpect(jsonPath("$.responsaveis[0].nome").value("Ana Silva"));
        mvc.perform(como(rui, post("/api/v1/tarefas/" + textos + "/responsaveis"), atribuicao("rui@matriz.local")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responsaveis.length()").value(2));

        // Continuam lá ao reabrir a tarefa e o filtro por responsável encontra-a
        mvc.perform(como(ana, get("/api/v1/tarefas/" + textos)))
                .andExpect(jsonPath("$.responsaveis[0].nome").value("Ana Silva"))
                .andExpect(jsonPath("$.responsaveis[1].nome").value("Rui Costa"));
        long website = jdbc.queryForObject("SELECT projeto_id FROM tarefa WHERE id = ?", Long.class, textos);
        mvc.perform(como(ana, get("/api/v1/projetos/" + website + "/tarefas?responsavelId=" + utilizador("ana@matriz.local"))))
                .andExpect(jsonPath("$.total").value(3));
    }

    @Test
    void aMesmaAtribuicaoNaoFicaDuplicada() throws Exception {
        mvc.perform(como(ana, post("/api/v1/tarefas/" + maquetas + "/responsaveis"), atribuicao("rui@matriz.local")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Este utilizador já é responsável pela tarefa"));
        assertThat(atribuicoes(maquetas)).isEqualTo(2);
    }

    @Test
    void responsavelTemDeExistirESerMembro() throws Exception {
        // Joana existe mas não é membro; 999999 não existe: a mesma resposta
        JsonNode naoMembro = lerJson(mvc.perform(como(ana, post("/api/v1/tarefas/" + textos + "/responsaveis"),
                        atribuicao("joana@matriz.local")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("/erros/responsavel-nao-membro")));
        JsonNode inexistente = lerJson(mvc.perform(como(ana, post("/api/v1/tarefas/" + textos + "/responsaveis"),
                        Map.of("utilizadorId", "999999")))
                .andExpect(status().isUnprocessableEntity()));
        assertThat(inexistente.get("detail")).isEqualTo(naoMembro.get("detail"));
        assertThat(atribuicoes(textos)).isZero();
    }

    @Test
    void pedidoDeAtribuicaoInvalido() throws Exception {
        atribuirInvalido(Map.of(), "utilizadorId");
        atribuirInvalido(Map.of("utilizadorId", "abc"), "utilizadorId");
        atribuirInvalido(Map.of("utilizadorId", "1", "papel", "GESTOR"), "papel");
    }

    @Test
    void removerCadaAtribuicao() throws Exception {
        long ruiId = utilizador("rui@matriz.local");
        mvc.perform(como(rui, delete("/api/v1/tarefas/" + maquetas + "/responsaveis/" + ruiId)))
                .andExpect(status().isNoContent());
        mvc.perform(como(rui, get("/api/v1/tarefas/" + maquetas)))
                .andExpect(jsonPath("$.responsaveis.length()").value(1))
                .andExpect(jsonPath("$.responsaveis[0].nome").value("Ana Silva"));
        // Já não é responsável
        mvc.perform(como(rui, delete("/api/v1/tarefas/" + maquetas + "/responsaveis/" + ruiId)))
                .andExpect(status().isNotFound());
        // O utilizador continua membro e a tarefa continua a existir
        assertThat(jdbc.queryForObject("SELECT count(*) FROM membro_projeto WHERE utilizador_id = ?", Long.class, ruiId))
                .isEqualTo(2);
    }

    @Test
    void naoMembroNaoGereResponsaveis() throws Exception {
        long anaId = utilizador("ana@matriz.local");
        mvc.perform(como(joana, post("/api/v1/tarefas/" + textos + "/responsaveis"), atribuicao("ana@matriz.local")))
                .andExpect(status().isNotFound());
        mvc.perform(como(joana, delete("/api/v1/tarefas/" + maquetas + "/responsaveis/" + anaId)))
                .andExpect(status().isNotFound());
        assertThat(atribuicoes(textos)).isZero();
        assertThat(atribuicoes(maquetas)).isEqualTo(2);
    }

    // --- Comentários --------------------------------------------------------------------------------------

    @Test
    void listaComentariosDoMaisAntigoParaOMaisRecenteComAutorEData() throws Exception {
        mvc.perform(como(rui, get("/api/v1/tarefas/" + maquetas + "/comentarios")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.itens[0].autor.nome").value("Rui Costa"))
                .andExpect(jsonPath("$.itens[0].conteudo").value("A primeira versão está pronta para revisão."))
                .andExpect(jsonPath("$.itens[1].autor.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.itens[1].criadoEm").isNotEmpty());
        mvc.perform(como(rui, get("/api/v1/tarefas/" + textos + "/comentarios")))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void autorEDataVemDoServidor() throws Exception {
        OffsetDateTime antes = OffsetDateTime.now().minusSeconds(5);
        JsonNode criado = lerJson(mvc.perform(como(rui, post("/api/v1/tarefas/" + maquetas + "/comentarios"),
                        Map.of("conteudo", "  Revisto, falta o rodapé.  ")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.conteudo").value("Revisto, falta o rodapé."))
                .andExpect(jsonPath("$.autor.nome").value("Rui Costa")));
        assertThat(OffsetDateTime.parse(criado.get("criadoEm").asText())).isAfter(antes);

        // O cliente não pode indicar o autor nem a data
        comentarInvalido(Map.of("conteudo", "Olá", "autorId", "1"), "autorId");
        comentarInvalido(Map.of("conteudo", "Olá", "criadoEm", "2020-01-01T00:00:00Z"), "criadoEm");

        // Continua lá ao reabrir, por último, e conta na tarefa
        mvc.perform(como(ana, get("/api/v1/tarefas/" + maquetas + "/comentarios")))
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.itens[2].id").value(criado.get("id").asText()));
        mvc.perform(como(ana, get("/api/v1/tarefas/" + maquetas)))
                .andExpect(jsonPath("$.totalComentarios").value(3));
    }

    @Test
    void validacaoDoConteudo() throws Exception {
        comentarInvalido(Map.of(), "conteudo");
        comentarInvalido(Map.of("conteudo", ""), "conteudo");
        comentarInvalido(Map.of("conteudo", "   \n  "), "conteudo");
        comentarInvalido(Map.of("conteudo", "x".repeat(2001)), "conteudo");
        mvc.perform(como(ana, post("/api/v1/tarefas/" + textos + "/comentarios"), Map.of("conteudo", "x".repeat(2000))))
                .andExpect(status().isCreated());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM comentario WHERE tarefa_id = ?", Long.class, textos))
                .isEqualTo(1);
    }

    @Test
    void naoMembroNaoVeNemComenta() throws Exception {
        mvc.perform(como(joana, get("/api/v1/tarefas/" + maquetas + "/comentarios"))).andExpect(status().isNotFound());
        mvc.perform(como(joana, post("/api/v1/tarefas/" + maquetas + "/comentarios"), Map.of("conteudo", "Intrusa")))
                .andExpect(status().isNotFound());
        mvc.perform(como(ana, get("/api/v1/tarefas/999999/comentarios"))).andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM comentario WHERE conteudo = 'Intrusa'", Long.class)).isZero();
    }

    // --- Auxiliares ---------------------------------------------------------------------------------------

    private Map<String, String> atribuicao(String email) {
        return Map.of("utilizadorId", String.valueOf(utilizador(email)));
    }

    private void atribuirInvalido(Map<String, Object> corpo, String campo) throws Exception {
        mvc.perform(como(ana, post("/api/v1/tarefas/" + textos + "/responsaveis"), corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value(campo));
    }

    private void comentarInvalido(Map<String, Object> corpo, String campo) throws Exception {
        mvc.perform(como(ana, post("/api/v1/tarefas/" + textos + "/comentarios"), corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/erros/validacao"))
                .andExpect(jsonPath("$.erros[0].campo").value(campo));
    }

    private long atribuicoes(long tarefa) {
        return jdbc.queryForObject("SELECT count(*) FROM tarefa_responsavel WHERE tarefa_id = ?", Long.class, tarefa);
    }

    private long idDe(String titulo) {
        return jdbc.queryForObject("SELECT id FROM tarefa WHERE titulo = ?", Long.class, titulo);
    }

    private long utilizador(String email) {
        return jdbc.queryForObject("SELECT id FROM utilizador WHERE email = ?", Long.class, email);
    }
}
