package pt.matriz.mappers;

import pt.matriz.domain.SessaoIniciada;
import pt.matriz.domain.Utilizador;
import pt.matriz.dtos.SessaoResposta;
import pt.matriz.dtos.UtilizadorResposta;

/** Converte utilizadores e sessões do domínio nos DTOs de {@code /auth/*}. */
public final class UtilizadorMapper {

    private UtilizadorMapper() {
    }

    public static UtilizadorResposta paraResposta(Utilizador utilizador) {
        return new UtilizadorResposta(String.valueOf(utilizador.getId()), utilizador.getNome(), utilizador.getEmail());
    }

    public static SessaoResposta paraResposta(SessaoIniciada sessao) {
        return new SessaoResposta(sessao.token().token(), sessao.token().expiraEm(),
                paraResposta(sessao.utilizador()));
    }
}
