package pt.matriz.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import pt.matriz.TesteIntegracao;

/**
 * Regressão do finding SEC-01 (Ficha 04, Ex. 5; docs/quality-security-review.md): o login de uma conta fica
 * bloqueado durante 15 minutos após 5 tentativas falhadas seguidas (RNF-11, P-29). As tentativas ficam gravadas
 * a sério (sem transação no teste), por isso cada teste usa a conta da Joana e repõe-na no fim.
 */
class BloqueioLoginTest extends TesteIntegracao {

    private static final String JOANA = "joana@matriz.local";

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    @Autowired
    JdbcTemplate jdbc;

    @AfterEach
    void reporJoana() {
        jdbc.update("UPDATE utilizador SET tentativas_falhadas = 0, bloqueado_ate = NULL WHERE email = ?", JOANA);
    }

    @Test
    void cincoFalhasSeguidasBloqueiamOLoginMesmoComAPasswordCerta() throws Exception {
        String passwordErrada = falhar(JOANA);
        for (int i = 2; i <= 4; i++) {
            falhar(JOANA);
        }
        // A 4.ª falha ainda não bloqueia
        assertThat(bloqueadoAte()).isNull();
        falhar(JOANA);

        // Bloqueada durante 15 minutos a partir da 5.ª falha
        Instant agora = Instant.now();
        assertThat(Duration.between(agora, bloqueadoAte().toInstant()))
                .isBetween(Duration.ofMinutes(14), Duration.ofMinutes(15));

        // Com a password certa: a mesma resposta de uma password errada (não revela o bloqueio)
        String comPasswordCerta = pedirLogin(JOANA, PASSWORD_DEMO).andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        assertThat(comPasswordCerta).isEqualTo(passwordErrada);

        // Tentar durante o bloqueio não o prolonga
        OffsetDateTime antes = bloqueadoAte();
        falhar(JOANA);
        assertThat(bloqueadoAte()).isEqualTo(antes);
    }

    @Test
    void depoisDoBloqueioOLoginVoltaAFuncionarERepoeAContagem() throws Exception {
        for (int i = 1; i <= 5; i++) {
            falhar(JOANA);
        }
        // Simula o fim dos 15 minutos
        jdbc.update("UPDATE utilizador SET bloqueado_ate = now() - interval '1 second' WHERE email = ?", JOANA);

        pedirLogin(JOANA, PASSWORD_DEMO).andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT tentativas_falhadas FROM utilizador WHERE email = ?", Integer.class,
                JOANA)).isZero();
        assertThat(bloqueadoAte()).isNull();
    }

    @Test
    void umLoginCertoRepoeAContagemPorqueAsFalhasTemDeSerSeguidas() throws Exception {
        for (int i = 1; i <= 4; i++) {
            falhar(JOANA);
        }
        pedirLogin(JOANA, PASSWORD_DEMO).andExpect(status().isOk());
        for (int i = 1; i <= 4; i++) {
            falhar(JOANA);
        }
        pedirLogin(JOANA, PASSWORD_DEMO).andExpect(status().isOk());
        assertThat(bloqueadoAte()).isNull();
    }

    @Test
    void oBloqueioEPorContaEEmailsInexistentesNaoBloqueiamNada() throws Exception {
        for (int i = 1; i <= 5; i++) {
            falhar(JOANA);
        }
        for (int i = 1; i <= 6; i++) {
            falhar("ninguem@matriz.local");
        }
        // Outra conta continua a entrar
        pedirLogin("ana@matriz.local", PASSWORD_DEMO).andExpect(status().isOk());
        pedirLogin(JOANA, PASSWORD_DEMO).andExpect(status().isUnauthorized());
    }

    /** Uma tentativa com password errada; devolve o corpo da resposta 401. */
    private String falhar(String email) throws Exception {
        return pedirLogin(email, "errada").andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
    }

    private OffsetDateTime bloqueadoAte() {
        return jdbc.queryForObject("SELECT bloqueado_ate FROM utilizador WHERE email = ?", OffsetDateTime.class,
                JOANA);
    }

    private ResultActions pedirLogin(String email, String password) throws Exception {
        String corpo = json.writeValueAsString(Map.of("email", email, "password", password));
        return mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }
}
