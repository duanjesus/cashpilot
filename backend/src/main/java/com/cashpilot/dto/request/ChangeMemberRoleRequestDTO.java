package com.cashpilot.dto.request;

import com.cashpilot.entity.enums.FamilyRole;
import jakarta.validation.constraints.NotNull;

public record ChangeMemberRoleRequestDTO(
        @NotNull(message = "O papel é obrigatório")
        FamilyRole papel
) {
}
