package pt.matriz.identidade;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.matriz.comum.ErroApi;
import pt.matriz.identidade.ServicoTokens.TokenEmitido;

@Service
public class ServicoAutenticacao {

    private final UtilizadorRepositorio utilizadores;
    private final PasswordEncoder passwords;
    private final ServicoTokens tokens;
    /** Usado quando o email não existe, para o login demorar o mesmo e não revelar que contas existem. */
    private final String hashFicticio;

    public ServicoAutenticacao(UtilizadorRepositorio utilizadores, PasswordEncoder passwords, ServicoTokens tokens) {
        this.utilizadores = utilizadores;
        this.passwords = passwords;
        this.tokens = tokens;
        this.hashFicticio = passwords.encode("sem-conta");
    }

    @Transactional(readOnly = true)
    public SessaoIniciada entrar(String email, String password) {
        Utilizador utilizador = utilizadores.findByEmailIgnoreCase(email.trim()).orElse(null);
        if (utilizador == null) {
            passwords.matches(password, hashFicticio);
            throw ErroApi.credenciaisInvalidas();
        }
        if (!passwords.matches(password, utilizador.getPasswordHash())) {
            throw ErroApi.credenciaisInvalidas();
        }
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

    public record SessaoIniciada(TokenEmitido token, Utilizador utilizador) {
    }
}
