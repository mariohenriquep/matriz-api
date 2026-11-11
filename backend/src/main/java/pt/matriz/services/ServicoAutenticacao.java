package pt.matriz.services;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.matriz.domain.SessaoIniciada;
import pt.matriz.domain.Utilizador;
import pt.matriz.exceptions.ErroApi;
import pt.matriz.repositories.UtilizadorRepositorio;
import pt.matriz.security.ServicoTokens;

/** Login, utilizador autenticado e logout (Ficha 03, Ex. 3). */
@Service
public class ServicoAutenticacao {

    private final UtilizadorRepositorio utilizadores;
    private final PasswordEncoder passwords;
    private final ServicoTokens tokens;
    /** Usado quando o email não existe, para o login demorar o mesmo e não revelar que contas existem. */
    private final String hashFicticio;
    private final Clock relogio;

    /** RNF-11 (P-29): ao fim de 5 tentativas falhadas seguidas, o login da conta fica bloqueado 15 minutos. */
    static final int MAXIMO_TENTATIVAS = 5;
    static final Duration DURACAO_BLOQUEIO = Duration.ofMinutes(15);

    public ServicoAutenticacao(UtilizadorRepositorio utilizadores, PasswordEncoder passwords, ServicoTokens tokens,
            Clock relogio) {
        this.utilizadores = utilizadores;
        this.passwords = passwords;
        this.tokens = tokens;
        this.relogio = relogio;
        this.hashFicticio = passwords.encode("sem-conta");
    }

    /**
     * Sem transação à volta de propósito: a tentativa falhada tem de ficar gravada mesmo quando o login termina
     * com erro. Durante o bloqueio, a resposta é a mesma de uma password errada (não revela o bloqueio nem se a
     * password estava certa) e a tentativa não prolonga o bloqueio (Ficha 04, Ex. 5, SEC-01).
     */
    public SessaoIniciada entrar(String email, String password) {
        Utilizador utilizador = utilizadores.findByEmailIgnoreCase(email.trim()).orElse(null);
        if (utilizador == null) {
            passwords.matches(password, hashFicticio);
            throw ErroApi.credenciaisInvalidas();
        }
        Instant agora = relogio.instant();
        if (utilizador.bloqueadoEm(agora)) {
            passwords.matches(password, hashFicticio); // o mesmo tempo de resposta
            throw ErroApi.credenciaisInvalidas();
        }
        if (!passwords.matches(password, utilizador.getPasswordHash())) {
            utilizadores.registarTentativaFalhada(utilizador.getId(), MAXIMO_TENTATIVAS,
                    agora.plus(DURACAO_BLOQUEIO).atOffset(ZoneOffset.UTC));
            throw ErroApi.credenciaisInvalidas();
        }
        utilizadores.limparTentativas(utilizador.getId());
        return new SessaoIniciada(tokens.emitir(utilizador), utilizador);
    }

    @Transactional(readOnly = true)
    public Utilizador obter(long id) {
        return utilizadores.findById(id).orElseThrow(() -> ErroApi.naoAutenticado("A sessão já não é válida"));
    }

    /** Invalida todos os tokens já emitidos para o utilizador. */
    @Transactional
    public void sair(long id) {
        utilizadores.findById(id).ifPresent(Utilizador::invalidarTokens);
    }
}
