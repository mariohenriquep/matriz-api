package pt.matriz.services;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import pt.matriz.exceptions.ErroApi;

/**
 * Regras dos dados de uma conta nova (registo público, versão 0.8.0): nome, email e password.
 * Cada regra recusa com um erro de validação (400) no campo certo; nenhuma depende de a conta já existir,
 * por isso as respostas não revelam que emails estão registados.
 */
final class RegrasConta {

    static final int MAX_NOME = 100;
    static final int MAX_EMAIL = 254;
    static final int MAX_LOCAL_EMAIL = 64;
    /** P-23: pelo menos 8 caracteres. */
    static final int MIN_PASSWORD = 8;
    static final int MAX_PASSWORD = 64;
    /** O bcrypt só usa os primeiros 72 bytes: acima disso, duas passwords diferentes dariam o mesmo hash. */
    static final int MAX_BYTES_PASSWORD = 72;
    /** Passwords como "aaaaaaaa" ou "abababab" são recusadas. */
    static final int MIN_CARACTERES_DIFERENTES = 4;

    private static final CampoTexto NOME = CampoTexto.obrigatorio("nome", "O nome", MAX_NOME);

    /**
     * Uma só arroba, sem espaços nem caracteres de controlo, e um domínio com pelo menos um ponto
     * (o {@code @Email} do Bean Validation aceita "ana@local", que não recebe emails).
     */
    private static final Pattern EMAIL = Pattern.compile(
            "^[^@\\s\\p{Cntrl}]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$");

    /** Caracteres de controlo e de formatação invisíveis (ex.: U+202E, que inverte o texto) não entram no nome. */
    private static final Pattern INVISIVEIS = Pattern.compile("[\\p{Cc}\\p{Cf}]");

    /** Das passwords mais usadas em fugas de dados (com 8 ou mais caracteres), mais as óbvias para esta aplicação. */
    private static final Set<String> COMUNS = Set.of(
            "12345678", "123456789", "1234567890", "12341234", "11111111", "00000000", "87654321", "12121212",
            "password", "password1", "password123", "passw0rd", "qwertyui", "qwerty123", "qwertyuiop", "1q2w3e4r",
            "1qaz2wsx", "zaq12wsx", "asdfghjk", "asdfasdf", "iloveyou", "sunshine", "princess", "football",
            "baseball", "superman", "trustno1", "letmein1", "welcome1", "abcd1234", "abc12345", "aa123456",
            "changeme", "administrator", "admin123", "senha123", "palavrapasse", "portugal", "benfica1",
            "sporting", "matriz123", "matriz2026", "qwerty12", "q1w2e3r4", "monkey12", "dragon12", "master12");

    private RegrasConta() {
    }

    static String nome(String nome) {
        String limpo = NOME.validar(nome);
        if (INVISIVEIS.matcher(limpo).find()) {
            throw ErroApi.validacao("nome", "O nome tem caracteres inválidos");
        }
        return limpo;
    }

    /** O email limpo e em minúsculas, a forma com que fica guardado (CE-08). */
    static String email(String email) {
        String limpo = CampoTexto.limpo(email);
        if (limpo == null) {
            throw ErroApi.validacao("email", "O email é obrigatório");
        }
        String normalizado = limpo.toLowerCase(Locale.ROOT);
        if (normalizado.length() > MAX_EMAIL || !EMAIL.matcher(normalizado).matches()
                || normalizado.indexOf('@') > MAX_LOCAL_EMAIL) {
            throw ErroApi.validacao("email", "O email não é válido");
        }
        return normalizado;
    }

    /**
     * A password não é limpa (os espaços contam). Recusa as curtas, as longas demais para o bcrypt, as com poucos
     * caracteres diferentes, as muito comuns e as que contêm o email.
     */
    static void password(String password, String emailNormalizado) {
        if (password == null || password.isEmpty()) {
            throw ErroApi.validacao("password", "A password é obrigatória");
        }
        int caracteres = password.codePointCount(0, password.length());
        if (caracteres < MIN_PASSWORD) {
            throw ErroApi.validacao("password", "A password tem de ter pelo menos " + MIN_PASSWORD + " caracteres");
        }
        if (caracteres > MAX_PASSWORD || password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES_PASSWORD) {
            throw ErroApi.validacao("password", "A password tem no máximo " + MAX_PASSWORD + " caracteres");
        }
        if (password.codePoints().distinct().count() < MIN_CARACTERES_DIFERENTES) {
            throw ErroApi.validacao("password", "A password é demasiado repetitiva");
        }
        String minusculas = password.toLowerCase(Locale.ROOT);
        if (COMUNS.contains(minusculas)) {
            throw ErroApi.validacao("password", "Esta password é demasiado comum. Escolhe outra");
        }
        String local = emailNormalizado.substring(0, emailNormalizado.indexOf('@'));
        if (local.length() >= 3 && minusculas.contains(local)) {
            throw ErroApi.validacao("password", "A password não pode conter o teu email");
        }
    }
}
