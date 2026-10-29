package pt.matriz.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

/** Corpo do {@code POST /auth/login}. */
public record PedidoLogin(
        @NotBlank(message = "O email é obrigatório") @Email(message = "O email não é válido") String email,
        @NotEmpty(message = "A password é obrigatória") String password) {
}
