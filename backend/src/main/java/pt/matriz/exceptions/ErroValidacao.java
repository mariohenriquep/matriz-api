package pt.matriz.exceptions;

import org.springframework.http.HttpStatus;

/** Dados inválidos detetados no serviço; a resposta indica o campo, como nas validações por anotação. */
public class ErroValidacao extends ErroApi {

    private final String campo;

    ErroValidacao(String campo, String mensagem) {
        super(HttpStatus.BAD_REQUEST, "/erros/validacao", "Pedido inválido", mensagem);
        this.campo = campo;
    }

    public String campo() {
        return campo;
    }
}
