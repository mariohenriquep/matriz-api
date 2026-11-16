package pt.matriz.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

/** Janela fixa de pedidos por chave (versão 0.8.0). Testes unitários com um relógio controlado. */
class LimitadorPedidosTest {

    private Instant agora = Instant.parse("2026-11-16T10:00:00Z");

    private final Clock relogio = new Clock() {
        @Override
        public Instant instant() {
            return agora;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zona) {
            return this;
        }
    };

    @Test
    void deixaPassarAteAoMaximoERecusaDepoisComOTempoQueFalta() {
        LimitadorPedidos limite = new LimitadorPedidos(3, Duration.ofMinutes(1), 100, relogio);
        assertThat(limite.tentar("1.1.1.1")).isEmpty();
        agora = agora.plusSeconds(10);
        assertThat(limite.tentar("1.1.1.1")).isEmpty();
        assertThat(limite.tentar("1.1.1.1")).isEmpty();
        assertThat(limite.tentar("1.1.1.1")).contains(Duration.ofSeconds(50));
        // Outro IP tem a sua própria contagem
        assertThat(limite.tentar("2.2.2.2")).isEmpty();
    }

    @Test
    void aJanelaRecomecaQuandoTermina() {
        LimitadorPedidos limite = new LimitadorPedidos(1, Duration.ofMinutes(1), 100, relogio);
        assertThat(limite.tentar("1.1.1.1")).isEmpty();
        assertThat(limite.tentar("1.1.1.1")).isPresent();
        agora = agora.plusSeconds(60);
        assertThat(limite.tentar("1.1.1.1")).isEmpty();
    }

    @Test
    void insistirDuranteOBloqueioNaoOProlonga() {
        LimitadorPedidos limite = new LimitadorPedidos(1, Duration.ofMinutes(1), 100, relogio);
        limite.tentar("1.1.1.1");
        for (int i = 0; i < 1000; i++) {
            limite.tentar("1.1.1.1");
        }
        agora = agora.plusSeconds(59);
        assertThat(limite.tentar("1.1.1.1")).contains(Duration.ofSeconds(1));
        agora = agora.plusSeconds(1);
        assertThat(limite.tentar("1.1.1.1")).isEmpty();
    }

    @Test
    void comOMapaCheioApagaAsJanelasTerminadasOuRecusaChavesNovas() {
        LimitadorPedidos limite = new LimitadorPedidos(5, Duration.ofMinutes(1), 2, relogio);
        limite.tentar("1.1.1.1");
        limite.tentar("2.2.2.2");
        // Cheio e nenhuma janela terminou: um IP novo é recusado, os conhecidos continuam
        assertThat(limite.tentar("3.3.3.3")).isPresent();
        assertThat(limite.tentar("1.1.1.1")).isEmpty();
        // Terminadas as janelas, há espaço outra vez
        agora = agora.plusSeconds(61);
        assertThat(limite.tentar("3.3.3.3")).isEmpty();
        assertThat(limite.chavesGuardadas()).isEqualTo(1);
    }

    @Test
    void recusaConfiguracoesInvalidas() {
        assertThatThrownBy(() -> new LimitadorPedidos(0, Duration.ofMinutes(1), 1, relogio))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new LimitadorPedidos(1, Duration.ZERO, 1, relogio))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new LimitadorPedidos(1, Duration.ofMinutes(1), 0, relogio))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
