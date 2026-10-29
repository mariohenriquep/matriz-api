package pt.matriz.identidade;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import pt.matriz.comum.ErroApi;

/**
 * Identidade de quem faz o pedido, obtida sempre do token validado. Os controladores nunca aceitam do
 * cliente o dono do projeto, o criador da tarefa nem o autor do comentário (Ficha 03, Ex. 3).
 */
@Component
public class UtilizadorAtual {

    public long id() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao instanceof JwtAuthenticationToken jwt) {
            return Long.parseLong(jwt.getToken().getSubject());
        }
        throw ErroApi.naoAutenticado("É preciso iniciar sessão");
    }
}
