package pt.matriz.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Erro de negócio ou de acesso, convertido em Problem Details (RFC 9457) por {@link TratamentoErros}.
 * Os tipos e títulos seguem {@code components/responses} do openapi.yaml.
 */
public class ErroApi extends RuntimeException {

    private final HttpStatus estado;
    private final String tipo;
    private final String titulo;

    protected ErroApi(HttpStatus estado, String tipo, String titulo, String detalhe) {
        super(detalhe);
        this.estado = estado;
        this.tipo = tipo;
        this.titulo = titulo;
    }

    public static ErroApi naoAutenticado(String detalhe) {
        return new ErroApi(HttpStatus.UNAUTHORIZED, "/erros/nao-autenticado", "Não autenticado", detalhe);
    }

    public static ErroApi credenciaisInvalidas() {
        return new ErroApi(HttpStatus.UNAUTHORIZED, "/erros/credenciais-invalidas", "Não autenticado",
                "Email ou password incorretos");
    }

    /** 404, usado também quando o utilizador não é membro do projeto (CE-07). */
    public static ErroApi naoEncontrado(String detalhe) {
        return new ErroApi(HttpStatus.NOT_FOUND, "/erros/nao-encontrado", "Não encontrado", detalhe);
    }

    public static ErroApi semPermissao(String detalhe) {
        return new ErroApi(HttpStatus.FORBIDDEN, "/erros/sem-permissao", "Sem permissão", detalhe);
    }

    public static ErroApi conflito(String detalhe) {
        return new ErroApi(HttpStatus.CONFLICT, "/erros/conflito", "Conflito", detalhe);
    }

    public static ErroApi regraDeNegocio(String tipo, String detalhe) {
        return new ErroApi(HttpStatus.UNPROCESSABLE_ENTITY, tipo, "Regra de negócio", detalhe);
    }

    /** 400 para validações feitas no serviço (as anotações de Bean Validation dão o mesmo formato). */
    public static ErroValidacao validacao(String campo, String mensagem) {
        return new ErroValidacao(campo, mensagem);
    }

    public HttpStatus estado() {
        return estado;
    }

    public String tipo() {
        return tipo;
    }

    public String titulo() {
        return titulo;
    }
}
