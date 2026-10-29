package pt.matriz.identidade;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Recusa tokens de utilizadores que entretanto terminaram sessão: a versão no token tem de ser igual à
 * {@code versao_token} guardada (incrementada no logout).
 */
class ValidadorVersaoToken implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2TokenValidatorResult INVALIDO =
            OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Sessão terminada", null));

    private final UtilizadorRepositorio utilizadores;

    ValidadorVersaoToken(UtilizadorRepositorio utilizadores) {
        this.utilizadores = utilizadores;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        Object versao = token.getClaim(ServicoTokens.CLAIM_VERSAO);
        long id;
        try {
            id = Long.parseLong(token.getSubject());
        } catch (NumberFormatException e) {
            return INVALIDO;
        }
        if (!(versao instanceof Number numero)) {
            return INVALIDO;
        }
        return utilizadores.findById(id)
                .filter(utilizador -> utilizador.getVersaoToken() == numero.intValue())
                .map(utilizador -> OAuth2TokenValidatorResult.success())
                .orElse(INVALIDO);
    }
}
