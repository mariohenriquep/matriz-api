package pt.matriz.mappers;

import pt.matriz.domain.SessaoIniciada;
import pt.matriz.domain.Utilizador;
import pt.matriz.dtos.ContaResposta;
import pt.matriz.dtos.SessaoResposta;
import pt.matriz.dtos.UtilizadorResposta;

/** Converte utilizadores e sessões do domínio nos DTOs de {@code /auth/*}. */
public final class UtilizadorMapper {

    private UtilizadorMapper() {
    }

    public static UtilizadorResposta paraResposta(Utilizador utilizador) {
        return new UtilizadorResposta(String.valueOf(utilizador.getId()), utilizador.getNome(), utilizador.getEmail());
    }

    /** O utilizador autenticado, com o plano (só para o próprio). */
    public static ContaResposta paraConta(Utilizador utilizador) {
        return new ContaResposta(String.valueOf(utilizador.getId()), utilizador.getNome(), utilizador.getEmail(),
                utilizador.getPlano().name(), utilizador.getPlano().podeCriarProjetos());
    }

    public static SessaoResposta paraResposta(SessaoIniciada sessao) {
        return new SessaoResposta(sessao.token().token(), sessao.token().expiraEm(),
                paraConta(sessao.utilizador()));
    }
}
