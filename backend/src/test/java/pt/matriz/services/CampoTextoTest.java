package pt.matriz.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import pt.matriz.exceptions.ErroValidacao;

/**
 * Testes unitários da validação dos campos de texto (Ficha 04, Ex. 2; casos T-01 a T-04 de docs/test-plan.md).
 * Sem Spring nem base de dados. Os valores esperados vêm dos requisitos e não do código: título obrigatório,
 * não só com espaços (RF-08, US-06 CA3) e com 1 a 200 caracteres (docs/constraints.md, openapi.yaml).
 */
class CampoTextoTest {

    /** Limite do requisito, escrito à mão de propósito: se o código mudar, o teste deve dar por isso. */
    private static final int LIMITE_REQUISITO = 200;

    /** O mesmo campo que o ServicoTarefas usa para o título. */
    private static final CampoTexto TITULO =
            CampoTexto.obrigatorio("titulo", "O título", ServicoTarefas.MAX_TITULO);

    @Test
    void oLimiteDoServicoEOdoRequisito() {
        assertThat(ServicoTarefas.MAX_TITULO).isEqualTo(LIMITE_REQUISITO);
    }

    @Nested
    class TituloValido {

        @Test
        void devolveOTitulo() {
            assertThat(TITULO.validar("Rever logótipo")).isEqualTo("Rever logótipo");
        }

        @Test
        void retiraOsEspacosDasPontasMasNaoOsDoMeio() {
            assertThat(TITULO.validar("  Rever   o logótipo \t\n")).isEqualTo("Rever   o logótipo");
            // Espaços não separáveis, comuns ao copiar de páginas web (regressão do Ex. 8 da Ficha 03)
            assertThat(TITULO.validar(" Rever ")).isEqualTo("Rever");
        }
    }

    @Nested
    class TituloInvalido {

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   ", "\t\n", "  ", "   　 ", "﻿"})
        void vazioOuSoComEspacosERecusado(String titulo) {
            assertThatThrownBy(() -> TITULO.validar(titulo))
                    .isInstanceOfSatisfying(ErroValidacao.class, erro -> {
                        assertThat(erro.campo()).isEqualTo("titulo");
                        assertThat(erro.getMessage()).isEqualTo("O título é obrigatório");
                    });
        }
    }

    @Nested
    class ValoresLimite {

        @Test
        void umCaracterEAceite() {
            assertThat(TITULO.validar("x")).isEqualTo("x");
        }

        @Test
        void duzentosCaracteresSaoAceites() {
            String titulo = "x".repeat(LIMITE_REQUISITO);
            assertThat(TITULO.validar(titulo)).isEqualTo(titulo);
        }

        @Test
        void duzentosEUmCaracteresSaoRecusados() {
            assertThatThrownBy(() -> TITULO.validar("x".repeat(LIMITE_REQUISITO + 1)))
                    .isInstanceOfSatisfying(ErroValidacao.class, erro -> {
                        assertThat(erro.campo()).isEqualTo("titulo");
                        assertThat(erro.getMessage()).isEqualTo("O título tem no máximo 200 caracteres");
                    });
        }

        @Test
        void oLimiteContaDepoisDeRetirarOsEspacosDasPontas() {
            String titulo = "x".repeat(LIMITE_REQUISITO);
            assertThat(TITULO.validar("   " + titulo + "   ")).isEqualTo(titulo);
        }

        @Test
        void letrasAcentuadasContamComoUmCaracter() {
            String titulo = "é".repeat(LIMITE_REQUISITO);
            assertThat(TITULO.validar(titulo)).isEqualTo(titulo);
        }

        /**
         * T-04 / P-6: o requisito fala em caracteres. Um emoji como 📌 é um só caracter, mas ocupa duas unidades
         * UTF-16 numa String Java. O PostgreSQL (VARCHAR(200)) conta caracteres e aceita este título.
         */
        @Test
        void duzentosEmojisSaoDuzentosCaracteres() {
            String titulo = "📌".repeat(LIMITE_REQUISITO);
            assertThat(titulo.codePointCount(0, titulo.length())).isEqualTo(LIMITE_REQUISITO);
            assertThat(TITULO.validar(titulo)).isEqualTo(titulo);
        }

        @Test
        void duzentosEUmEmojisSaoRecusados() {
            assertThatThrownBy(() -> TITULO.validar("📌".repeat(LIMITE_REQUISITO + 1)))
                    .isInstanceOf(ErroValidacao.class);
        }
    }

    @Nested
    class CampoOpcional {

        private final CampoTexto descricao = CampoTexto.opcional("descricao", "A descrição", 2000);

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "  ", " "})
        void vazioFicaSemValor(String valor) {
            assertThat(descricao.validar(valor)).isNull();
        }

        @Test
        void acimaDoMaximoERecusado() {
            assertThatThrownBy(() -> descricao.validar("x".repeat(2001)))
                    .isInstanceOfSatisfying(ErroValidacao.class,
                            erro -> assertThat(erro.campo()).isEqualTo("descricao"));
        }
    }
}
