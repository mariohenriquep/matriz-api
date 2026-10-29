package pt.matriz.comum;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracaoComum {

    /** Relógio da aplicação; nos testes pode ser substituído para simular a passagem do tempo. */
    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }
}
