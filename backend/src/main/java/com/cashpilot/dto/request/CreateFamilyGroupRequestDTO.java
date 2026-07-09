package com.cashpilot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFamilyGroupRequestDTO(
        @NotBlank(message = "O nome do grupo é obrigatório")
        @Size(max = 150, message = "O nome do grupo deve ter no máximo 150 caracteres")
        String nome
) {
}
