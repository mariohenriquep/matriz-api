package pt.matriz.dtos;

/**
 * Corpo do {@code POST /auth/registo}. As regras de cada campo ficam no serviço (mensagens no mesmo formato
 * das anotações). {@code site} é um campo que o formulário esconde das pessoas: só um robô o preenche.
 */
public record PedidoRegisto(String nome, String email, String password, String site) {
}
