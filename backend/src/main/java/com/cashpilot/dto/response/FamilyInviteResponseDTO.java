package com.cashpilot.dto.response;

import com.cashpilot.entity.enums.FamilyRole;
import com.cashpilot.entity.enums.InviteStatus;

import java.time.LocalDateTime;

public record FamilyInviteResponseDTO(
        Long id,
        Long familyGroupId,
        String familyGroupNome,
        String invitedEmail,
        FamilyRole invitedRole,
        InviteStatus status,
        LocalDateTime createdAt
) {
}
