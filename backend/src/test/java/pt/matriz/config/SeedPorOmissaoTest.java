package pt.matriz.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Regressão do finding SEC-02 (Ficha 04, Ex. 5; docs/quality-security-review.md): com a configuração real
 * (application.yml) e sem MATRIZ_SEED_ATIVO, as contas de demonstração não são criadas. Sem base de dados:
 * só verifica se o componente do seed entra no contexto.
 */
class SeedPorOmissaoTest {

    private final ApplicationContextRunner contexto = new ApplicationContextRunner()
            .withInitializer(ctx -> {
                var fontes = ctx.getEnvironment().getPropertySources();
                // Ignora as variáveis de ambiente da máquina onde o teste corre
                fontes.remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
                try {
                    new YamlPropertySourceLoader().load("application.yml", new ClassPathResource("application.yml"))
                            .forEach(fontes::addLast);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            })
            .withBean(JdbcTemplate.class, () -> mock(JdbcTemplate.class))
            .withBean(PasswordEncoder.class, () -> mock(PasswordEncoder.class))
            .withUserConfiguration(DadosDemonstracao.class);

    @Test
    void semAVariavelOSeedFicaDesligado() {
        contexto.run(ctx -> assertThat(ctx).doesNotHaveBean(DadosDemonstracao.class));
    }

    @Test
    void comMatrizSeedAtivoTrueOSeedLiga() {
        contexto.withPropertyValues("MATRIZ_SEED_ATIVO=true")
                .run(ctx -> assertThat(ctx).hasSingleBean(DadosDemonstracao.class));
    }
}
