package com.cashpilot.dto.response;

import com.cashpilot.entity.enums.FamilyRole;

import java.util.List;

public record FamilyGroupResponseDTO(
        Long id,
        String nome,
        Long ownerUserId,
        FamilyRole papelDoUsuarioAtual,
        List<FamilyGroupMemberResponseDTO> membros
) {
}
