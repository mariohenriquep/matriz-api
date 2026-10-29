package pt.matriz.dtos;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import pt.matriz.domain.EstadoTarefa;
import pt.matriz.domain.Prioridade;

/**
 * Corpo do {@code PATCH /tarefas/{id}}. Só os campos enviados mudam; {@code descricao} e {@code dataLimite}
 * a {@code null} apagam o valor.
 */
public class AlteracaoTarefa {

    private String titulo;
    private String descricao;
    private EstadoTarefa estado;
    private Prioridade prioridade;
    private LocalDate dataLimite;
    private boolean temTitulo;
    private boolean temDescricao;
    private boolean temEstado;
    private boolean temPrioridade;
    private boolean temDataLimite;

    @JsonProperty("titulo")
    void setTitulo(String titulo) {
        this.titulo = titulo;
        this.temTitulo = true;
    }

    @JsonProperty("descricao")
    void setDescricao(String descricao) {
        this.descricao = descricao;
        this.temDescricao = true;
    }

    @JsonProperty("estado")
    void setEstado(EstadoTarefa estado) {
        this.estado = estado;
        this.temEstado = true;
    }

    @JsonProperty("prioridade")
    void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
        this.temPrioridade = true;
    }

    @JsonProperty("dataLimite")
    void setDataLimite(LocalDate dataLimite) {
        this.dataLimite = dataLimite;
        this.temDataLimite = true;
    }

    public String titulo() {
        return titulo;
    }

    public String descricao() {
        return descricao;
    }

    public EstadoTarefa estado() {
        return estado;
    }

    public Prioridade prioridade() {
        return prioridade;
    }

    public LocalDate dataLimite() {
        return dataLimite;
    }

    public boolean temTitulo() {
        return temTitulo;
    }

    public boolean temDescricao() {
        return temDescricao;
    }

    public boolean temEstado() {
        return temEstado;
    }

    public boolean temPrioridade() {
        return temPrioridade;
    }

    public boolean temDataLimite() {
        return temDataLimite;
    }
}
