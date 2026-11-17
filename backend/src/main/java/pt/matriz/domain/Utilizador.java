package pt.matriz.domain;

import java.time.Instant;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "utilizador")
public class Utilizador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    /** Incrementada no logout: os tokens com uma versão anterior deixam de ser aceites. */
    @Column(name = "versao_token", nullable = false)
    private int versaoToken;

    /** Só muda na base de dados (ainda não há pagamentos). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private Plano plano;

    @Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime criadoEm;

    /**
     * Bloqueio do login (RNF-11). Só de leitura na entidade: estas colunas mudam por atualizações atómicas no
     * repositório, para um logout (que grava a entidade) nunca repor um valor antigo.
     */
    @Column(name = "tentativas_falhadas", nullable = false, insertable = false, updatable = false)
    private int tentativasFalhadas;

    @Column(name = "bloqueado_ate", insertable = false, updatable = false)
    private OffsetDateTime bloqueadoAte;

    protected Utilizador() {
    }

    /** Conta nova do registo público: plano gratuito, sem projetos. Os dados já vêm validados. */
    public static Utilizador novo(String nome, String email, String passwordHash) {
        Utilizador utilizador = new Utilizador();
        utilizador.nome = nome;
        utilizador.email = email;
        utilizador.passwordHash = passwordHash;
        utilizador.plano = Plano.GRATUITO;
        return utilizador;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Plano getPlano() {
        return plano;
    }

    public int getVersaoToken() {
        return versaoToken;
    }

    public int getTentativasFalhadas() {
        return tentativasFalhadas;
    }

    /** O login está bloqueado neste instante. */
    public boolean bloqueadoEm(Instant agora) {
        return bloqueadoAte != null && agora.isBefore(bloqueadoAte.toInstant());
    }

    public void invalidarTokens() {
        versaoToken++;
    }
}
