package pt.matriz.identidade;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * Emite os tokens JWT (HS256). Validade fixa contada a partir do login (P-28): 8 horas por defeito.
 * O token leva o id do utilizador ({@code sub}) e a versão de token ({@code ver}) em vigor no login.
 */
@Service
public class ServicoTokens {

    static final String CLAIM_VERSAO = "ver";
    static final String EMISSOR = "matriz";
    private static final int TAMANHO_MINIMO_SEGREDO = 32;

    private final SecretKey chave;
    private final JwtEncoder codificador;
    private final Duration validade;
    private final Clock relogio;

    public ServicoTokens(@Value("${matriz.jwt.segredo:}") String segredo,
            @Value("${matriz.jwt.validade:PT8H}") Duration validade, Clock relogio) {
        if (segredo == null || segredo.getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_SEGREDO) {
            throw new IllegalStateException("MATRIZ_JWT_SEGREDO tem de estar definida no .env, com pelo menos "
                    + TAMANHO_MINIMO_SEGREDO + " caracteres.");
        }
        this.chave = new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.codificador = new NimbusJwtEncoder(new ImmutableSecret<>(chave));
        this.validade = validade;
        this.relogio = relogio;
    }

    public TokenEmitido emitir(Utilizador utilizador) {
        Instant agora = relogio.instant();
        Instant expiraEm = agora.plus(validade);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(EMISSOR)
                .subject(String.valueOf(utilizador.getId()))
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .claim(CLAIM_VERSAO, utilizador.getVersaoToken())
                .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = codificador.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
        return new TokenEmitido(token, expiraEm);
    }

    SecretKey chave() {
        return chave;
    }

    public record TokenEmitido(String token, Instant expiraEm) {
    }
}
