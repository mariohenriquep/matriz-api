package pt.matriz.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import pt.matriz.domain.Utilizador;
import pt.matriz.repositories.UtilizadorRepositorio;

/**
 * Registo público de contas (versão 0.8.0).
 *
 * <p>Segurança:
 * <ul>
 *   <li>A conta nova fica com o plano gratuito: não cria projetos e só vê um projeto quando um Gestor a adiciona.
 *       Registar-se não dá acesso a nada que já exista.</li>
 *   <li>A resposta é sempre a mesma, exista ou não uma conta com o email, e o tempo também (o hash da password
 *       calcula-se sempre). Assim o registo não serve para descobrir que emails estão registados.</li>
 *   <li>Um campo escondido no formulário ({@code site}) apanha robôs: se vier preenchido, nada é criado e a
 *       resposta é a mesma.</li>
 *   <li>O limite de registos por IP e no total fica no filtro {@code FiltroLimitePedidos}.</li>
 * </ul>
 */
@Service
public class ServicoRegisto {

    private static final Logger log = LoggerFactory.getLogger(ServicoRegisto.class);

    private final UtilizadorRepositorio utilizadores;
    private final PasswordEncoder passwords;

    public ServicoRegisto(UtilizadorRepositorio utilizadores, PasswordEncoder passwords) {
        this.utilizadores = utilizadores;
        this.passwords = passwords;
    }

    /**
     * Valida os dados (400 se forem inválidos) e cria a conta se o email ainda não estiver registado.
     * Não diz se criou: quem chama responde sempre o mesmo.
     */
    public void registar(String nome, String email, String password, String armadilha) {
        String nomeValido = RegrasConta.nome(nome);
        String emailValido = RegrasConta.email(email);
        RegrasConta.password(password, emailValido);
        String hash = passwords.encode(password);

        if (armadilha != null && !armadilha.isBlank()) {
            log.info("Registo ignorado: o campo escondido do formulário veio preenchido.");
            return;
        }
        if (utilizadores.findByEmailIgnoreCase(emailValido).isPresent()) {
            return;
        }
        try {
            utilizadores.saveAndFlush(Utilizador.novo(nomeValido, emailValido, hash));
        } catch (DataIntegrityViolationException e) {
            // Dois registos com o mesmo email ao mesmo tempo: o índice único fica com o primeiro
            log.debug("Registo em simultâneo com o mesmo email ignorado.");
        }
    }
}
