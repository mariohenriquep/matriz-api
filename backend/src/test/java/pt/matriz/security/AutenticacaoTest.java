package pt.matriz.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import pt.matriz.TesteIntegracao;
import pt.matriz.repositories.UtilizadorRepositorio;

/** Ficha 03, Ex. 3: login, perfil, logout e pedidos sem sessão válida. */
class AutenticacaoTest extends TesteIntegracao {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    @Autowired
    UtilizadorRepositorio utilizadores;

    @Test
    void loginValidoDevolveTokenComValidadeDe8Horas() throws Exception {
        Instant antes = Instant.now();
        JsonNode sessao = entrar("ana@matriz.local", PASSWORD_DEMO);

        assertThat(sessao.get("token").asText()).isNotBlank();
        assertThat(sessao.at("/utilizador/email").asText()).isEqualTo("ana@matriz.local");
        assertThat(sessao.at("/utilizador/nome").asText()).isEqualTo("Ana Silva");
        Instant expiraEm = Instant.parse(sessao.get("expiraEm").asText());
        assertThat(Duration.between(antes, expiraEm)).isBetween(Duration.ofHours(8).minusMinutes(1),
                Duration.ofHours(8).plusMinutes(1));
    }

    @Test
    void emailNaoDistingueMaiusculas() throws Exception {
        assertThat(entrar("ANA@Matriz.Local", PASSWORD_DEMO).at("/utilizador/email").asText())
                .isEqualTo("ana@matriz.local");
    }

    @Test
    void passwordErradaEEmailInexistenteDaoAMesmaRespostaGenerica() throws Exception {
        String passwordErrada = pedirLogin("ana@matriz.local", "errada")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Email ou password incorretos"))
                .andReturn().getResponse().getContentAsString();
        String emailInexistente = pedirLogin("ninguem@matriz.local", PASSWORD_DEMO)
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertThat(emailInexistente).isEqualTo(passwordErrada);
    }

    @Test
    void loginComDadosInvalidosDa400ComOsCampos() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nao-e-um-email\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/erros/validacao"))
                .andExpect(jsonPath("$.erros[?(@.campo == 'email')]").exists())
                .andExpect(jsonPath("$.erros[?(@.campo == 'password')]").exists());
    }

    @Test
    void loginComCamposDesconhecidosDa400() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@matriz.local\",\"password\":\"x\",\"admin\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void euDevolveOUtilizadorDoToken() throws Exception {
        String tokenAna = entrar("ana@matriz.local", PASSWORD_DEMO).get("token").asText();
        String tokenRui = entrar("rui@matriz.local", PASSWORD_DEMO).get("token").asText();

        mvc.perform(get("/api/v1/auth/eu").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@matriz.local"))
                .andExpect(jsonPath("$.password_hash").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        mvc.perform(get("/api/v1/auth/eu").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenRui))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("rui@matriz.local"));
    }

    @Test
    void pedidoSemTokenDa401NoFormatoDoContrato() throws Exception {
        mvc.perform(get("/api/v1/auth/eu"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString("application/problem+json")))
                .andExpect(jsonPath("$.type").value("/erros/nao-autenticado"))
                .andExpect(jsonPath("$.title").value("Não autenticado"))
                .andExpect(jsonPath("$.detail").value("É preciso iniciar sessão."));
    }

    @Test
    void qualquerRotaDaApiExigeToken() throws Exception {
        mvc.perform(get("/api/v1/projetos")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/logout")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenAdulteradoDa401() throws Exception {
        String token = entrar("ana@matriz.local", PASSWORD_DEMO).get("token").asText();
        String adulterado = token.substring(0, token.length() - 4) + "AAAA";

        mvc.perform(get("/api/v1/auth/eu").header(HttpHeaders.AUTHORIZATION, "Bearer " + adulterado))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("A sessão expirou ou já não é válida. Inicia sessão novamente."));
    }

    @Test
    void tokenAssinadoComOutroSegredoDa401() throws Exception {
        String token = tokenPara(idDe("ana@matriz.local"), 0, Instant.now().plusSeconds(3600),
                "outro-segredo-qualquer-com-mais-de-32-caracteres");
        mvc.perform(get("/api/v1/auth/eu").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenExpiradoDa401() throws Exception {
        long ana = idDe("ana@matriz.local");
        int versao = utilizadores.findById(ana).orElseThrow().getVersaoToken();
        String expirado = tokenPara(ana, versao, Instant.now().minus(Duration.ofHours(1)), SEGREDO_JWT);
        String valido = tokenPara(ana, versao, Instant.now().plus(Duration.ofHours(1)), SEGREDO_JWT);

        mvc.perform(get("/api/v1/auth/eu").header(HttpHeaders.AUTHORIZATION, "Bearer " + expirado))
                .andExpect(status().isUnauthorized());
        // Controlo: o mesmo token, só com outra data, é aceite
        mvc.perform(get("/api/v1/auth/eu").header(HttpHeaders.AUTHORIZATION, "Bearer " + valido))
                .andExpect(status().isOk());
    }

    @Test
    void logoutInvalidaTodosOsTokensDoUtilizador() throws Exception {
        String primeiro = entrar("joana@matriz.local", PASSWORD_DEMO).get("token").asText();
        String segundo = entrar("joana@matriz.local", PASSWORD_DEMO).get("token").asText();
        String doRui = entrar("rui@matriz.local", PASSWORD_DEMO).get("token").asText();

        mvc.perform(post("/api/v1/auth/logout").header(HttpHeaders.AUTHORIZATION, "Bearer " + primeiro))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/auth/eu").header(HttpHeaders.AUTHORIZATION, "Bearer " + primeiro))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/auth/eu").header(HttpHeaders.AUTHORIZATION, "Bearer " + segundo))
                .andExpect(status().isUnauthorized());
        // Os tokens de outros utilizadores não são afetados
        mvc.perform(get("/api/v1/auth/eu").header(HttpHeaders.AUTHORIZATION, "Bearer " + doRui))
                .andExpect(status().isOk());
        // Um novo login volta a funcionar
        String novo = entrar("joana@matriz.local", PASSWORD_DEMO).get("token").asText();
        mvc.perform(get("/api/v1/auth/eu").header(HttpHeaders.AUTHORIZATION, "Bearer " + novo))
                .andExpect(status().isOk());
    }

    @Test
    void healthEPublicoMasSemDetalhes() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void corsSoAceitaAsOrigensConfiguradas() throws Exception {
        mvc.perform(options("/api/v1/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
        mvc.perform(options("/api/v1/auth/login")
                        .header(HttpHeaders.ORIGIN, "https://site-malicioso.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    private ResultActions pedirLogin(String email, String password) throws Exception {
        String corpo = json.writeValueAsString(Map.of("email", email, "password", password));
        return mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private JsonNode entrar(String email, String password) throws Exception {
        String resposta = pedirLogin(email, password).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resposta);
    }

    private long idDe(String email) {
        return utilizadores.findByEmailIgnoreCase(email).orElseThrow().getId();
    }

    private static String tokenPara(long utilizador, int versao, Instant expiraEm, String segredo) {
        var chave = new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ServicoTokens.EMISSOR)
                .subject(String.valueOf(utilizador))
                .issuedAt(expiraEm.minus(Duration.ofHours(8)))
                .expiresAt(expiraEm)
                .claim(ServicoTokens.CLAIM_VERSAO, versao)
                .build();
        return new NimbusJwtEncoder(new ImmutableSecret<>(chave))
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
