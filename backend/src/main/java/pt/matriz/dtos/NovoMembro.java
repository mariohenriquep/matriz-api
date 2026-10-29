package pt.matriz.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Corpo do {@code POST /projetos/{id}/membros}. */
public record NovoMembro(
        @NotBlank(message = "O email é obrigatório") @Email(message = "O email não é válido") String email) {
}
