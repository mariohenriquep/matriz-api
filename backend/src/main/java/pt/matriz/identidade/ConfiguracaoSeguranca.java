package pt.matriz.identidade;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import pt.matriz.comum.Problemas;

/**
 * Segurança da API (Ficha 03, Ex. 3).
 *
 * <ul>
 *   <li>Sem sessão no servidor nem cookies: cada pedido traz {@code Authorization: Bearer <token>}.
 *       Por isso a proteção CSRF não se aplica e fica desligada.</li>
 *   <li>Só {@code POST /api/v1/auth/login} e {@code /actuator/health} são públicos.</li>
 *   <li>CORS só para as origens de {@code MATRIZ_CORS_ORIGENS}.</li>
 * </ul>
 */
@Configuration
public class ConfiguracaoSeguranca {

    @Bean
    SecurityFilterChain seguranca(HttpSecurity http, ObjectMapper json) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> { }) // usa o bean corsConfigurationSource
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .authorizeHttpRequests(pedidos -> pedidos
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(recurso -> recurso
                        .jwt(jwt -> { })
                        .authenticationEntryPoint((pedido, resposta, erro) -> naoAutenticado(pedido, resposta, json)))
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint((pedido, resposta, erro) -> naoAutenticado(pedido, resposta, json)));
        return http.build();
    }

    @Bean
    JwtDecoder descodificadorJwt(ServicoTokens tokens, UtilizadorRepositorio utilizadores) {
        NimbusJwtDecoder descodificador = NimbusJwtDecoder.withSecretKey(tokens.chave())
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        descodificador.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(ServicoTokens.EMISSOR),
                new ValidadorVersaoToken(utilizadores)));
        return descodificador;
    }

    @Bean
    PasswordEncoder codificadorPasswords() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${matriz.cors.origens}") List<String> origens) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(origens);
        cors.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE));
        cors.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/api/**", cors);
        return fonte;
    }

    /** 401 no formato do contrato. Não distingue "sem token" de "token inválido" além da mensagem. */
    private static void naoAutenticado(HttpServletRequest pedido, HttpServletResponse resposta, ObjectMapper json)
            throws IOException {
        boolean trouxeToken = pedido.getHeader(HttpHeaders.AUTHORIZATION) != null;
        String detalhe = trouxeToken
                ? "A sessão expirou ou já não é válida. Inicia sessão novamente."
                : "É preciso iniciar sessão.";
        ProblemDetail problema = Problemas.criar(HttpStatus.UNAUTHORIZED, "/erros/nao-autenticado",
                "Não autenticado", detalhe);
        resposta.setStatus(HttpStatus.UNAUTHORIZED.value());
        resposta.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        resposta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        resposta.setCharacterEncoding("UTF-8");
        json.writeValue(resposta.getOutputStream(), problema);
    }
}
