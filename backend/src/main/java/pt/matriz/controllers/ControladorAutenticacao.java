package pt.matriz.controllers;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import pt.matriz.dtos.ContaResposta;
import pt.matriz.dtos.PedidoLogin;
import pt.matriz.dtos.PedidoRegisto;
import pt.matriz.dtos.RegistoResposta;
import pt.matriz.dtos.SessaoResposta;
import pt.matriz.mappers.UtilizadorMapper;
import pt.matriz.security.UtilizadorAtual;
import pt.matriz.services.ServicoAutenticacao;
import pt.matriz.services.ServicoRegisto;

/** {@code /auth/*} do openapi.yaml: criar conta, iniciar sessão, consultar o utilizador e terminar sessão. */
@RestController
@RequestMapping("/api/v1/auth")
public class ControladorAutenticacao {

    /** A mesma para conta criada, email já registado ou pedido de um robô. */
    static final String MENSAGEM_REGISTO = "Pedido de registo recebido. Se este email ainda não tinha conta, "
            + "a conta foi criada e já podes iniciar sessão.";

    private final ServicoAutenticacao autenticacao;
    private final ServicoRegisto registo;
    private final UtilizadorAtual utilizadorAtual;

    public ControladorAutenticacao(ServicoAutenticacao autenticacao, ServicoRegisto registo,
            UtilizadorAtual utilizadorAtual) {
        this.autenticacao = autenticacao;
        this.registo = registo;
        this.utilizadorAtual = utilizadorAtual;
    }

    /** 202 sempre que os dados são válidos: a resposta não revela se o email já tinha conta. */
    @PostMapping("/registo")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RegistoResposta registar(@RequestBody PedidoRegisto pedido) {
        registo.registar(pedido.nome(), pedido.email(), pedido.password(), pedido.site());
        return new RegistoResposta(MENSAGEM_REGISTO);
    }

    @PostMapping("/login")
    public SessaoResposta entrar(@Valid @RequestBody PedidoLogin pedido) {
        return UtilizadorMapper.paraResposta(autenticacao.entrar(pedido.email(), pedido.password()));
    }

    @GetMapping("/eu")
    public ContaResposta eu() {
        return UtilizadorMapper.paraConta(autenticacao.obter(utilizadorAtual.id()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sair() {
        autenticacao.sair(utilizadorAtual.id());
    }
}
