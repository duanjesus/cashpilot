package com.cashpilot.dto.response;

import com.cashpilot.entity.enums.FamilyRole;

public record FamilyGroupMemberResponseDTO(
        Long userId,
        String nome,
        String email,
        FamilyRole papel
) {
}
