package pt.matriz;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base dos testes de integração: aplicação completa contra um PostgreSQL 16 real (Testcontainers), com o seed
 * de demonstração. O contentor e o contexto Spring são partilhados por todas as classes de teste.
 */
@SpringBootTest(properties = {
        "matriz.seed.password=" + TesteIntegracao.PASSWORD_DEMO,
        "matriz.jwt.segredo=" + TesteIntegracao.SEGREDO_JWT
})
@AutoConfigureMockMvc
@Import(TesteIntegracao.BaseDeDados.class)
public abstract class TesteIntegracao {

    public static final String PASSWORD_DEMO = "password-de-teste";
    public static final String SEGREDO_JWT = "segredo-so-para-testes-com-mais-de-32-caracteres";

    @TestConfiguration(proxyBeanMethods = false)
    static class BaseDeDados {

        @Bean
        @ServiceConnection
        PostgreSQLContainer<?> postgres() {
            return new PostgreSQLContainer<>("postgres:16-alpine");
        }
    }
}
