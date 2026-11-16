package pt.matriz.domain;

/**
 * Plano da conta (versão 0.8.0). Decide o que a conta pode fazer além de participar nos projetos para que um
 * Gestor a convida. Ainda não há pagamentos: o plano muda-se diretamente na base de dados.
 */
public enum Plano {
    /** Conta criada pelo registo público: entra em projetos só quando um Gestor a adiciona. */
    GRATUITO,
    /** Pode também criar projetos e ficar Gestor. Será um plano pago. */
    PROFISSIONAL;

    public boolean podeCriarProjetos() {
        return this == PROFISSIONAL;
    }
}
