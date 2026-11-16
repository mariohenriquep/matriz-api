package pt.matriz.dtos;

/** Resposta do registo: sempre a mesma mensagem, para não revelar se o email já tinha conta. */
public record RegistoResposta(String mensagem) {
}
