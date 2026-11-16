package pt.matriz.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import pt.matriz.TesteIntegracao;

/**
 * Limites de pedidos ao login e ao registo, de ponta a ponta (versão 0.8.0). Limites baixos só nesta classe
 * (contexto Spring próprio) e dois proxies, como no Render: cada teste usa IPs diferentes no X-Forwarded-For.
 * Os pedidos usam dados inválidos ou contas inexistentes, para não criar contas nem bloquear as do seed.
 */
@TestPropertySource(properties = {
        "matriz.limites.login.pedidos=3",
        "matriz.limites.registo.pedidos=2",
        "matriz.limites.registo.total=3",
        "matriz.limites.proxies-confiaveis=2"
})
class LimitesPedidosTest extends TesteIntegracao {

    private static final String ORIGEM = "http://localhost:5173";
    private static final String CLOUDFLARE = "104.22.17.40";

    @Autowired
    MockMvc mvc;

    private ResultActions login(String ip) throws Exception {
        return mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .header("X-Forwarded-For", ip + ", " + CLOUDFLARE).header(HttpHeaders.ORIGIN, ORIGEM)
                .content("{\"email\":\"ninguem@exemplo.pt\",\"password\":\"qualquer-coisa\"}"));
    }

    private ResultActions registo(String ip) throws Exception {
        return mvc.perform(post("/api/v1/auth/registo").contentType(MediaType.APPLICATION_JSON)
                .header("X-Forwarded-For", ip + ", " + CLOUDFLARE).header(HttpHeaders.ORIGIN, ORIGEM)
                .content("{}"));
    }

    @Test
    void oLoginDeUmIpFicaLimitadoE429TemRetryAfterECors() throws Exception {
        for (int i = 0; i < 3; i++) {
            login("198.51.100.1").andExpect(status().isUnauthorized());
        }
        String corpo = login("198.51.100.1")
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGEM))
                .andExpect(jsonPath("$.type").value("/erros/demasiados-pedidos"))
                .andReturn().getResponse().getContentAsString();
        assertThat(corpo).contains("Tenta novamente daqui a 1 minuto.");
        long segundos = Long.parseLong(login("198.51.100.1").andReturn().getResponse()
                .getHeader(HttpHeaders.RETRY_AFTER));
        assertThat(segundos).isBetween(1L, 60L);

        // Outro IP não é afetado, mesmo que tente fingir ser outro com um X-Forwarded-For inventado
        login("198.51.100.2").andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .header("X-Forwarded-For", "203.0.113.99, 198.51.100.1, " + CLOUDFLARE)
                        .content("{\"email\":\"ninguem@exemplo.pt\",\"password\":\"x\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void oRegistoTemLimitePorIpEUmLimiteTotal() throws Exception {
        registo("192.0.2.1").andExpect(status().isBadRequest());
        registo("192.0.2.1").andExpect(status().isBadRequest());
        registo("192.0.2.1").andExpect(status().isTooManyRequests());
        // Outro IP ainda pode, até se esgotar o total de 3 registos por janela
        registo("192.0.2.2").andExpect(status().isBadRequest());
        registo("192.0.2.3").andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.detail").value("Demasiadas tentativas. Tenta novamente daqui a 60 minutos."));
    }

    @Test
    void asOutrasRotasNaoContam() throws Exception {
        for (int i = 0; i < 5; i++) {
            mvc.perform(get("/actuator/health").header("X-Forwarded-For", "192.0.2.50, " + CLOUDFLARE))
                    .andExpect(status().isOk());
        }
    }
}
