package pt.matriz;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Base dos testes por HTTP: login com as contas do seed e pedidos autenticados. */
public abstract class ApiTeste extends TesteIntegracao {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper json;

    protected String token(String email) throws Exception {
        String resposta = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "password", PASSWORD_DEMO))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resposta).get("token").asText();
    }

    /** Junta o token e, se houver corpo, serializa-o em JSON. */
    protected MockHttpServletRequestBuilder como(String token, MockHttpServletRequestBuilder pedido, Object corpo)
            throws Exception {
        pedido.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        if (corpo != null) {
            pedido.contentType(MediaType.APPLICATION_JSON)
                    .content(corpo instanceof String texto ? texto : json.writeValueAsString(corpo));
        }
        return pedido;
    }

    protected MockHttpServletRequestBuilder como(String token, MockHttpServletRequestBuilder pedido) throws Exception {
        return como(token, pedido, null);
    }

    protected JsonNode lerJson(ResultActions resultado) throws Exception {
        return json.readTree(resultado.andReturn().getResponse().getContentAsString());
    }
}
