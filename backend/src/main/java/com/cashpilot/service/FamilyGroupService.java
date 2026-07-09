package com.cashpilot.service;

import com.cashpilot.dto.request.ChangeMemberRoleRequestDTO;
import com.cashpilot.dto.request.CreateFamilyGroupRequestDTO;
import com.cashpilot.dto.request.FamilyInviteRequestDTO;
import com.cashpilot.dto.response.FamilyGroupResponseDTO;
import com.cashpilot.dto.response.FamilyInviteResponseDTO;

import java.util.List;
import java.util.Optional;

public interface FamilyGroupService {

    FamilyGroupResponseDTO create(CreateFamilyGroupRequestDTO dto);

    Optional<FamilyGroupResponseDTO> findMyGroup();

    FamilyInviteResponseDTO invite(FamilyInviteRequestDTO dto);

    List<FamilyInviteResponseDTO> findPendingInvitesForCurrentUser();

    FamilyGroupResponseDTO acceptInvite(Long inviteId);

    void declineInvite(Long inviteId);

    FamilyGroupResponseDTO changeMemberRole(Long userId, ChangeMemberRoleRequestDTO dto);

    void removeMember(Long userId);

    void deleteGroup();

}
