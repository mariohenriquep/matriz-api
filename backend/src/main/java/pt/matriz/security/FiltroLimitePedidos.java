package pt.matriz.security;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import pt.matriz.exceptions.Problemas;

/**
 * Limita os pedidos às rotas públicas de autenticação (versão 0.8.0), antes de chegarem ao controlador:
 * <ul>
 *   <li>{@code POST /api/v1/auth/login}: por IP (trava quem tenta muitas passwords em várias contas; o bloqueio
 *       por conta, RNF-11, trava quem insiste numa só);</li>
 *   <li>{@code POST /api/v1/auth/registo}: por IP e também no total, para travar a criação de contas em massa
 *       mesmo vinda de muitos IPs.</li>
 * </ul>
 * Acima do limite responde 429 com {@code Retry-After}. Fica depois do filtro de CORS, para o browser conseguir
 * ler a resposta.
 */
public class FiltroLimitePedidos extends OncePerRequestFilter {

    static final String LOGIN = "/api/v1/auth/login";
    static final String REGISTO = "/api/v1/auth/registo";
    private static final String TOTAL = "*";

    private static final Logger log = LoggerFactory.getLogger(FiltroLimitePedidos.class);

    private final LimitadorPedidos loginPorIp;
    private final LimitadorPedidos registoPorIp;
    private final LimitadorPedidos registoTotal;
    private final EnderecoCliente endereco;
    private final ObjectMapper json;

    public FiltroLimitePedidos(LimitadorPedidos loginPorIp, LimitadorPedidos registoPorIp,
            LimitadorPedidos registoTotal, EnderecoCliente endereco, ObjectMapper json) {
        this.loginPorIp = loginPorIp;
        this.registoPorIp = registoPorIp;
        this.registoTotal = registoTotal;
        this.endereco = endereco;
        this.json = json;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest pedido) {
        String rota = pedido.getRequestURI().substring(pedido.getContextPath().length());
        return !"POST".equals(pedido.getMethod()) || !List.of(LOGIN, REGISTO).contains(rota);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest pedido, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        String ip = endereco.de(pedido);
        boolean registo = pedido.getRequestURI().endsWith(REGISTO);
        Optional<Duration> esperar = registo
                ? registoPorIp.tentar(ip).or(() -> registoTotal.tentar(TOTAL))
                : loginPorIp.tentar(ip);
        if (esperar.isPresent()) {
            log.warn("Limite de pedidos atingido em {} para {}", registo ? "registo" : "login", ip);
            recusar(resposta, esperar.get());
            return;
        }
        cadeia.doFilter(pedido, resposta);
    }

    private void recusar(HttpServletResponse resposta, Duration esperar) throws IOException {
        long segundos = Math.max(1, (esperar.toMillis() + 999) / 1000);
        long minutos = (segundos + 59) / 60;
        ProblemDetail problema = Problemas.criar(HttpStatus.TOO_MANY_REQUESTS, "/erros/demasiados-pedidos",
                "Demasiados pedidos", "Demasiadas tentativas. Tenta novamente daqui a "
                        + (minutos == 1 ? "1 minuto." : minutos + " minutos."));
        resposta.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        resposta.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(segundos));
        resposta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        resposta.setCharacterEncoding("UTF-8");
        json.writeValue(resposta.getOutputStream(), problema);
    }
}
