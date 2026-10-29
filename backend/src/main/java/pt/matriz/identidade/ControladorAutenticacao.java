package pt.matriz.identidade;

import java.time.Instant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import pt.matriz.identidade.ServicoAutenticacao.SessaoIniciada;

/** {@code /auth/*} do openapi.yaml: iniciar sessão, consultar o utilizador autenticado e terminar sessão. */
@RestController
@RequestMapping("/api/v1/auth")
public class ControladorAutenticacao {

    private final ServicoAutenticacao autenticacao;
    private final UtilizadorAtual utilizadorAtual;

    public ControladorAutenticacao(ServicoAutenticacao autenticacao, UtilizadorAtual utilizadorAtual) {
        this.autenticacao = autenticacao;
        this.utilizadorAtual = utilizadorAtual;
    }

    @PostMapping("/login")
    public SessaoResposta entrar(@Valid @RequestBody PedidoLogin pedido) {
        SessaoIniciada sessao = autenticacao.entrar(pedido.email(), pedido.password());
        return new SessaoResposta(sessao.token().token(), sessao.token().expiraEm(),
                UtilizadorResposta.de(sessao.utilizador()));
    }

    @GetMapping("/eu")
    public UtilizadorResposta eu() {
        return UtilizadorResposta.de(autenticacao.obter(utilizadorAtual.id()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sair() {
        autenticacao.sair(utilizadorAtual.id());
    }

    public record PedidoLogin(
            @NotBlank(message = "O email é obrigatório") @Email(message = "O email não é válido") String email,
            @NotEmpty(message = "A password é obrigatória") String password) {
    }

    public record SessaoResposta(String token, Instant expiraEm, UtilizadorResposta utilizador) {
    }

    public record UtilizadorResposta(String id, String nome, String email) {

        static UtilizadorResposta de(Utilizador utilizador) {
            return new UtilizadorResposta(String.valueOf(utilizador.getId()), utilizador.getNome(),
                    utilizador.getEmail());
        }
    }
}
