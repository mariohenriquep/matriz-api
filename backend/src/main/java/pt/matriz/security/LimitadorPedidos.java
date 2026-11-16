package pt.matriz.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limite de pedidos por chave (ex.: o IP de quem pede) numa janela fixa: no máximo {@code maximo} pedidos por
 * {@code janela}. Guarda as contagens em memória, o que chega para uma só instância do backend (o plano gratuito
 * do Render só tem uma); com várias instâncias, cada uma conta à parte.
 *
 * <p>Para a memória não crescer sem fim com IPs diferentes, as janelas já terminadas são apagadas quando o mapa
 * chega a {@code maximoChaves}. Se mesmo assim estiver cheio (muitos IPs diferentes na mesma janela, sinal de
 * ataque distribuído), os pedidos de chaves novas são recusados até haver espaço.
 */
public class LimitadorPedidos {

    private record Janela(Instant inicio, int pedidos) {
    }

    private final int maximo;
    private final Duration janela;
    private final int maximoChaves;
    private final Clock relogio;
    private final Map<String, Janela> janelas = new ConcurrentHashMap<>();

    public LimitadorPedidos(int maximo, Duration janela, int maximoChaves, Clock relogio) {
        if (maximo < 1 || janela.isNegative() || janela.isZero() || maximoChaves < 1) {
            throw new IllegalArgumentException("Limite inválido: " + maximo + " pedidos por " + janela);
        }
        this.maximo = maximo;
        this.janela = janela;
        this.maximoChaves = maximoChaves;
        this.relogio = relogio;
    }

    /**
     * Conta um pedido da chave. Vazio se o pedido pode seguir; caso contrário, quanto tempo falta para a janela
     * terminar (para o cabeçalho {@code Retry-After}).
     */
    public Optional<Duration> tentar(String chave) {
        Instant agora = relogio.instant();
        if (!janelas.containsKey(chave) && janelas.size() >= maximoChaves) {
            janelas.values().removeIf(j -> terminou(j, agora));
            if (janelas.size() >= maximoChaves) {
                return Optional.of(janela);
            }
        }
        Janela atual = janelas.compute(chave, (c, j) -> j == null || terminou(j, agora)
                ? new Janela(agora, 1)
                : new Janela(j.inicio(), Math.min(j.pedidos() + 1, maximo + 1)));
        if (atual.pedidos() <= maximo) {
            return Optional.empty();
        }
        return Optional.of(Duration.between(agora, atual.inicio().plus(janela)));
    }

    private boolean terminou(Janela j, Instant agora) {
        return !agora.isBefore(j.inicio().plus(janela));
    }

    int chavesGuardadas() {
        return janelas.size();
    }
}
