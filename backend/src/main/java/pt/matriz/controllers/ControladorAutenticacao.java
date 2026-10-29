package pt.matriz.controllers;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import pt.matriz.dtos.PedidoLogin;
import pt.matriz.dtos.SessaoResposta;
import pt.matriz.dtos.UtilizadorResposta;
import pt.matriz.mappers.UtilizadorMapper;
import pt.matriz.security.UtilizadorAtual;
import pt.matriz.services.ServicoAutenticacao;

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
        return UtilizadorMapper.paraResposta(autenticacao.entrar(pedido.email(), pedido.password()));
    }

    @GetMapping("/eu")
    public UtilizadorResposta eu() {
        return UtilizadorMapper.paraResposta(autenticacao.obter(utilizadorAtual.id()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sair() {
        autenticacao.sair(utilizadorAtual.id());
    }
}
