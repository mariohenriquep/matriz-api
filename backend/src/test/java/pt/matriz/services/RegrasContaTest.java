package pt.matriz.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import pt.matriz.exceptions.ErroValidacao;

/** Regras dos dados de uma conta nova (registo público, versão 0.8.0). Testes unitários, sem Spring. */
class RegrasContaTest {

    private static final String EMAIL = "nova.pessoa@exemplo.pt";

    @Test
    void oEmailFicaLimpoEEmMinusculas() {
        assertThat(RegrasConta.email("  Nova.Pessoa@Exemplo.PT ")).isEqualTo(EMAIL);
    }

    @ParameterizedTest
    @ValueSource(strings = { "sem-arroba", "ana@local", "ana@@exemplo.pt", "ana silva@exemplo.pt", "ana@exemplo..pt",
            "ana@-exemplo.pt", "@exemplo.pt", "ana@exemplo.pt\nBcc:x@y.pt", "ana@exem_plo.pt" })
    void recusaEmailsInvalidos(String email) {
        assertThatThrownBy(() -> RegrasConta.email(email)).isInstanceOf(ErroValidacao.class)
                .hasMessage("O email não é válido");
    }

    @Test
    void recusaEmailVazioOuDemasiadoLongo() {
        assertThatThrownBy(() -> RegrasConta.email("   ")).hasMessage("O email é obrigatório");
        assertThatThrownBy(() -> RegrasConta.email(null)).hasMessage("O email é obrigatório");
        assertThatThrownBy(() -> RegrasConta.email("a".repeat(65) + "@exemplo.pt")).hasMessage("O email não é válido");
        assertThatThrownBy(() -> RegrasConta.email("a@" + "b".repeat(250) + ".pt")).hasMessage("O email não é válido");
    }

    @Test
    void oNomeFicaLimpoERecusaCaracteresInvisiveis() {
        assertThat(RegrasConta.nome("  Nova Pessoa ")).isEqualTo("Nova Pessoa");
        assertThatThrownBy(() -> RegrasConta.nome("Ana‮avlиS")).hasMessage("O nome tem caracteres inválidos");
        assertThatThrownBy(() -> RegrasConta.nome(" ")).hasMessage("O nome é obrigatório");
        assertThatThrownBy(() -> RegrasConta.nome("a".repeat(101))).hasMessage("O nome tem no máximo 100 caracteres");
    }

    @Test
    void aceitaUmaPasswordRazoavel() {
        assertThatCode(() -> RegrasConta.password("cavalo correto bateria", EMAIL)).doesNotThrowAnyException();
        assertThatCode(() -> RegrasConta.password("Ab3$efgh", EMAIL)).doesNotThrowAnyException();
    }

    @Test
    void recusaPasswordsCurtasOuDemasiadoLongas() {
        assertThatThrownBy(() -> RegrasConta.password("Ab3$efg", EMAIL))
                .hasMessage("A password tem de ter pelo menos 8 caracteres");
        assertThatThrownBy(() -> RegrasConta.password("", EMAIL)).hasMessage("A password é obrigatória");
        assertThatThrownBy(() -> RegrasConta.password(null, EMAIL)).hasMessage("A password é obrigatória");
        assertThatThrownBy(() -> RegrasConta.password("Ab3$efgh".repeat(9), EMAIL))
                .hasMessage("A password tem no máximo 64 caracteres");
        // 30 caracteres, mas 90 bytes: o bcrypt ignoraria o que passa dos 72
        assertThatThrownBy(() -> RegrasConta.password("€".repeat(15) + "çãõéáíóúàâêôüñ1", EMAIL))
                .hasMessage("A password tem no máximo 64 caracteres");
    }

    @Test
    void contaCaracteresENaoUnidadesUtf16() {
        // 8 emojis são 8 caracteres (16 unidades UTF-16), dentro dos 72 bytes
        assertThatCode(() -> RegrasConta.password("😀😁😂🤣😃😄😅😆", EMAIL)).doesNotThrowAnyException();
        assertThatThrownBy(() -> RegrasConta.password("😀😁😂🤣", EMAIL))
                .hasMessage("A password tem de ter pelo menos 8 caracteres");
    }

    @ParameterizedTest
    @ValueSource(strings = { "aaaaaaaa", "abababab", "abcabcabc" })
    void recusaPasswordsRepetitivas(String password) {
        assertThatThrownBy(() -> RegrasConta.password(password, EMAIL))
                .hasMessage("A password é demasiado repetitiva");
    }

    @ParameterizedTest
    @ValueSource(strings = { "12345678", "Password", "PASSWORD123", "qwertyuiop", "Matriz2026" })
    void recusaPasswordsComuns(String password) {
        assertThatThrownBy(() -> RegrasConta.password(password, EMAIL))
                .hasMessage("Esta password é demasiado comum. Escolhe outra");
    }

    @Test
    void recusaPasswordsComOEmail() {
        assertThatThrownBy(() -> RegrasConta.password("Nova.Pessoa!2026", EMAIL))
                .hasMessage("A password não pode conter o teu email");
        // Partes locais muito curtas não contam (dariam falsos positivos)
        assertThatCode(() -> RegrasConta.password("rui-e-companhia", "ru@exemplo.pt")).doesNotThrowAnyException();
    }
}
