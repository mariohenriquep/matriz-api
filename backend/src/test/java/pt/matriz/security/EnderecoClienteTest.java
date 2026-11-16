package pt.matriz.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/** IP do cliente para os limites de pedidos (versão 0.8.0). */
class EnderecoClienteTest {

    private static MockHttpServletRequest pedido(String xff) {
        MockHttpServletRequest pedido = new MockHttpServletRequest();
        pedido.setRemoteAddr("10.0.0.7");
        if (xff != null) {
            pedido.addHeader("X-Forwarded-For", xff);
        }
        return pedido;
    }

    @Test
    void semProxiesUsaOEnderecoDaLigacaoEIgnoraOCabecalho() {
        assertThat(new EnderecoCliente(0).de(pedido("6.6.6.6"))).isEqualTo("10.0.0.7");
    }

    @Test
    void comDoisProxiesUsaAPenultimaEntradaENaoAQueOClienteInventou() {
        EnderecoCliente render = new EnderecoCliente(2);
        assertThat(render.de(pedido("99.61.165.29, 104.22.17.40"))).isEqualTo("99.61.165.29");
        // O cliente mandou um X-Forwarded-For falso: fica à esquerda e não é usado
        assertThat(render.de(pedido("6.6.6.6, 99.61.165.29, 104.22.17.40"))).isEqualTo("99.61.165.29");
    }

    @Test
    void comMenosEntradasDoQueProxiesOuSemCabecalho() {
        EnderecoCliente render = new EnderecoCliente(2);
        assertThat(render.de(pedido("99.61.165.29"))).isEqualTo("99.61.165.29");
        assertThat(render.de(pedido(null))).isEqualTo("10.0.0.7");
        assertThat(render.de(pedido(" "))).isEqualTo("10.0.0.7");
        assertThat(render.de(pedido(" , 104.22.17.40"))).isEqualTo("10.0.0.7");
    }

    @Test
    void recusaNumeroNegativo() {
        assertThatThrownBy(() -> new EnderecoCliente(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
