package pt.matriz.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import pt.matriz.ApiTeste;

/**
 * Ficha 03, Ex. 9 (revisão): todos os erros seguem o formato do contrato (Problem Details em português),
 * incluindo os que o Spring gera antes de chegar aos controladores.
 */
class TratamentoErrosTest extends ApiTeste {

    String ana;

    @BeforeEach
    void entrar() throws Exception {
        ana = token("ana@matriz.local");
    }

    @Test
    void rotaInexistenteDa404NoFormatoDoContrato() throws Exception {
        mvc.perform(get("/api/v1/nada").header(HttpHeaders.AUTHORIZATION, "Bearer " + ana))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString("application/problem+json")))
                .andExpect(jsonPath("$.type").value("/erros/nao-encontrado"))
                .andExpect(jsonPath("$.detail").value("Recurso não encontrado"));
    }

    @Test
    void metodoNaoSuportadoDa405() throws Exception {
        mvc.perform(put("/api/v1/projetos/1").header(HttpHeaders.AUTHORIZATION, "Bearer " + ana))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.type").value("/erros/metodo-nao-suportado"))
                .andExpect(jsonPath("$.title").value("Método não suportado"));
    }

    @Test
    void corpoQueNaoEJsonDa415() throws Exception {
        mvc.perform(post("/api/v1/projetos").header(HttpHeaders.AUTHORIZATION, "Bearer " + ana)
                        .contentType(MediaType.TEXT_PLAIN).content("Projeto"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.type").value("/erros/tipo-nao-suportado"));
    }

    @Test
    void erroInesperadoDa500SemDetalhesInternos() {
        ResponseEntity<ProblemDetail> resposta =
                new TratamentoErros().erroInesperado(new IllegalStateException("ligação à BD perdida: senha=segredo"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resposta.getBody().getType().toString()).isEqualTo("/erros/interno");
        assertThat(resposta.getBody().getDetail()).isEqualTo("Ocorreu um erro inesperado. Tenta novamente mais tarde.");
        assertThat(resposta.getBody().getDetail()).doesNotContain("segredo");
    }
}
