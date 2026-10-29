package pt.matriz.domain;

/**
 * Alterações pedidas a um projeto. Os indicadores {@code alterarNome} e {@code alterarDescricao} distinguem
 * um campo que não muda de um campo que passa a vazio ({@code descricao = null} apaga a descrição).
 */
public record EdicaoProjeto(boolean alterarNome, String nome, boolean alterarDescricao, String descricao) {

    public boolean vazia() {
        return !alterarNome && !alterarDescricao;
    }
}
