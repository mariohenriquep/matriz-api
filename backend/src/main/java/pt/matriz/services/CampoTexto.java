package pt.matriz.services;

import java.util.regex.Pattern;

import pt.matriz.exceptions.ErroApi;

/**
 * Regras de um campo de texto que vem do cliente: espaços nas pontas retirados, obrigatório ou opcional
 * e tamanho máximo. Cada serviço declara os seus campos como constantes (ex.: {@code TITULO}) e valida com
 * {@link #validar(String)}, em vez de repetir a mesma lógica em cada método (refatoração do Ex. 9).
 *
 * <p>{@link String#trim()} e {@link String#isBlank()} não tratam como espaço os espaços não separáveis
 * (U+00A0, U+2007, U+202F), que aparecem ao copiar texto de páginas web ou documentos. Sem esta limpeza,
 * um título só com esses caracteres passava a validação e ficava "em branco" (bug encontrado no Ex. 8).
 *
 * @param campo nome do campo no pedido, devolvido no erro de validação
 * @param nome como o campo aparece nas mensagens (ex.: "O título")
 * @param maximo número máximo de caracteres depois da limpeza
 * @param seVazio mensagem quando o campo é obrigatório e vem vazio; {@code null} se for opcional
 */
record CampoTexto(String campo, String nome, int maximo, String seVazio) {

    /** Nas pontas: o que o trim() já retirava (espaços e caracteres de controlo), separadores Unicode e BOM. */
    private static final Pattern PONTAS =
            Pattern.compile("^[\\p{Cntrl}\\s\\p{Z}\\uFEFF]+|[\\p{Cntrl}\\s\\p{Z}\\uFEFF]+$");

    static CampoTexto obrigatorio(String campo, String nome, int maximo) {
        return new CampoTexto(campo, nome, maximo, nome + " é obrigatório");
    }

    static CampoTexto obrigatorio(String campo, String nome, int maximo, String seVazio) {
        return new CampoTexto(campo, nome, maximo, seVazio);
    }

    static CampoTexto opcional(String campo, String nome, int maximo) {
        return new CampoTexto(campo, nome, maximo, null);
    }

    /**
     * O valor limpo. Vazio ou só com espaços: erro de validação se o campo for obrigatório, {@code null} se for
     * opcional. Acima do máximo: erro de validação.
     */
    String validar(String valor) {
        String limpo = limpo(valor);
        if (limpo == null) {
            if (seVazio != null) {
                throw ErroApi.validacao(campo, seVazio);
            }
            return null;
        }
        if (limpo.length() > maximo) {
            throw ErroApi.validacao(campo, nome + " tem no máximo " + maximo + " caracteres");
        }
        return limpo;
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
