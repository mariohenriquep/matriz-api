package pt.matriz.services;

import java.util.regex.Pattern;

/**
 * Limpeza dos textos que chegam do cliente.
 *
 * <p>{@link String#trim()} e {@link String#isBlank()} não tratam como espaço os espaços não separáveis
 * (U+00A0, U+2007, U+202F), que aparecem ao copiar texto de páginas web ou documentos. Sem esta limpeza,
 * um título só com esses caracteres passava a validação e ficava "em branco" (bug encontrado no Ex. 8).
 */
final class Texto {

    /** Nas pontas: o que o trim() já retirava (espaços e caracteres de controlo), separadores Unicode e BOM. */
    private static final Pattern PONTAS =
            Pattern.compile("^[\\p{Cntrl}\\s\\p{Z}\\uFEFF]+|[\\p{Cntrl}\\s\\p{Z}\\uFEFF]+$");

    private Texto() {
    }

    /** O texto sem espaços nas pontas; {@code null} se for {@code null} ou só tiver espaços. */
    static String limpo(String valor) {
        if (valor == null) {
            return null;
        }
        String limpo = PONTAS.matcher(valor).replaceAll("");
        return limpo.isEmpty() ? null : limpo;
    }
}
