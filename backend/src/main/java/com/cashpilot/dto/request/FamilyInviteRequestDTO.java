package com.cashpilot.dto.request;

import com.cashpilot.entity.enums.FamilyRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FamilyInviteRequestDTO(
        @NotBlank(message = "O e-mail do convidado é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotNull(message = "O papel do convidado é obrigatório")
        FamilyRole papel
) {
}
