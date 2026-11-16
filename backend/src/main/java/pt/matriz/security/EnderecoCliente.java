package pt.matriz.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * O IP de quem fez o pedido, para os limites de pedidos.
 *
 * <p>Sem proxies ({@code proxiesConfiaveis = 0}) é o endereço da ligação. Atrás de proxies, cada um acrescenta
 * à direita do cabeçalho {@code X-Forwarded-For} o endereço de quem lhe ligou; o que o cliente lá puser fica à
 * esquerda. Por isso conta-se a partir da direita e nunca se confia na entrada mais à esquerda (seria fácil de
 * falsificar). No Render há dois: o Cloudflare (acrescenta o IP do cliente) e o balanceador do Render
 * (acrescenta o do Cloudflare), logo o IP do cliente é a penúltima entrada ({@code MATRIZ_PROXIES_CONFIAVEIS=2}).
 */
public final class EnderecoCliente {

    private final int proxiesConfiaveis;

    public EnderecoCliente(int proxiesConfiaveis) {
        if (proxiesConfiaveis < 0) {
            throw new IllegalArgumentException("O número de proxies não pode ser negativo");
        }
        this.proxiesConfiaveis = proxiesConfiaveis;
    }

    public String de(HttpServletRequest pedido) {
        if (proxiesConfiaveis == 0) {
            return pedido.getRemoteAddr();
        }
        String cabecalho = pedido.getHeader("X-Forwarded-For");
        if (cabecalho == null || cabecalho.isBlank()) {
            return pedido.getRemoteAddr();
        }
        String[] entradas = cabecalho.split(",");
        // Menos entradas do que proxies: o pedido não passou por todos; usa-se a mais à esquerda que existe
        int indice = Math.max(0, entradas.length - proxiesConfiaveis);
        String ip = entradas[indice].trim();
        return ip.isEmpty() ? pedido.getRemoteAddr() : ip;
    }
}
