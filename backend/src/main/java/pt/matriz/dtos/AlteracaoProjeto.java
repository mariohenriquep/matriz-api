package pt.matriz.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Corpo do {@code PATCH /projetos/{id}}. Distingue um campo ausente (não muda) de um campo a {@code null}
 * ({@code descricao: null} apaga a descrição).
 */
public class AlteracaoProjeto {

    private String nome;
    private String descricao;
    private boolean temNome;
    private boolean temDescricao;

    @JsonProperty("nome")
    void setNome(String nome) {
        this.nome = nome;
        this.temNome = true;
    }

    @JsonProperty("descricao")
    void setDescricao(String descricao) {
        this.descricao = descricao;
        this.temDescricao = true;
    }

    public String nome() {
        return nome;
    }

    public String descricao() {
        return descricao;
    }

    public boolean temNome() {
        return temNome;
    }

    public boolean temDescricao() {
        return temDescricao;
    }
}
