package com.cashpilot.service.impl;

import com.cashpilot.dto.request.ChangeMemberRoleRequestDTO;
import com.cashpilot.dto.request.CreateFamilyGroupRequestDTO;
import com.cashpilot.dto.request.FamilyInviteRequestDTO;
import com.cashpilot.dto.response.FamilyGroupMemberResponseDTO;
import com.cashpilot.dto.response.FamilyGroupResponseDTO;
import com.cashpilot.dto.response.FamilyInviteResponseDTO;
import com.cashpilot.entity.FamilyGroup;
import com.cashpilot.entity.FamilyGroupMember;
import com.cashpilot.entity.FamilyInvite;
import com.cashpilot.entity.User;
import com.cashpilot.entity.enums.FamilyRole;
import com.cashpilot.entity.enums.InviteStatus;
import com.cashpilot.exception.BusinessException;
import com.cashpilot.exception.DuplicateResourceException;
import com.cashpilot.exception.ResourceNotFoundException;
import com.cashpilot.repository.FamilyGroupMemberRepository;
import com.cashpilot.repository.FamilyGroupRepository;
import com.cashpilot.repository.FamilyInviteRepository;
import com.cashpilot.repository.UserRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.FamilyGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class FamilyGroupServiceImpl implements FamilyGroupService {

    private final FamilyGroupRepository familyGroupRepository;
    private final FamilyGroupMemberRepository familyGroupMemberRepository;
    private final FamilyInviteRepository familyInviteRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public FamilyGroupResponseDTO create(CreateFamilyGroupRequestDTO dto) {
        User user = currentUserProvider.getCurrentUser();
        if (familyGroupMemberRepository.existsByUserId(user.getId())) {
            throw new DuplicateResourceException("Você já pertence a um grupo familiar");
        }

        FamilyGroup group = familyGroupRepository.save(
                FamilyGroup.builder().nome(dto.nome()).ownerUser(user).build());

        familyGroupMemberRepository.save(FamilyGroupMember.builder()
                .familyGroup(group)
                .user(user)
                .role(FamilyRole.OWNER)
                .joinedAt(LocalDateTime.now())
                .build());

        return toGroupResponse(group, FamilyRole.OWNER);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FamilyGroupResponseDTO> findMyGroup() {
        Long userId = currentUserProvider.getCurrentUserId();
        return familyGroupMemberRepository.findByUserId(userId)
                .map(membership -> toGroupResponse(membership.getFamilyGroup(), membership.getRole()));
    }

    @Override
    public FamilyInviteResponseDTO invite(FamilyInviteRequestDTO dto) {
        if (dto.papel() == FamilyRole.OWNER) {
            throw new BusinessException("Não é possível convidar alguém diretamente como owner");
        }
        FamilyGroupMember myMembership = requireOwnerMembership();
        FamilyGroup group = myMembership.getFamilyGroup();

        User invited = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new BusinessException("Nenhum usuário encontrado com este e-mail"));

        if (familyGroupMemberRepository.existsByUserId(invited.getId())) {
            throw new DuplicateResourceException("Este usuário já pertence a um grupo familiar");
        }
        if (familyInviteRepository.existsByFamilyGroupIdAndInvitedEmailIgnoreCaseAndStatus(
                group.getId(), dto.email(), InviteStatus.PENDENTE)) {
            throw new DuplicateResourceException("Já existe um convite pendente para este e-mail");
        }

        FamilyInvite invite = familyInviteRepository.save(FamilyInvite.builder()
                .familyGroup(group)
                .invitedEmail(dto.email())
                .invitedRole(dto.papel())
                .status(InviteStatus.PENDENTE)
                .build());

        return toInviteResponse(invite);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FamilyInviteResponseDTO> findPendingInvitesForCurrentUser() {
        String email = currentUserProvider.getCurrentUser().getEmail();
        return familyInviteRepository.findAllByInvitedEmailIgnoreCaseAndStatus(email, InviteStatus.PENDENTE).stream()
                .map(this::toInviteResponse)
                .toList();
    }

    @Override
    public FamilyGroupResponseDTO acceptInvite(Long inviteId) {
        User user = currentUserProvider.getCurrentUser();
        if (familyGroupMemberRepository.existsByUserId(user.getId())) {
            throw new BusinessException("Você já pertence a um grupo familiar — saia do grupo atual antes de aceitar um novo convite");
        }

        FamilyInvite invite = findOwnInvite(inviteId, user.getEmail());
        if (invite.getStatus() != InviteStatus.PENDENTE) {
            throw new BusinessException("Este convite já foi respondido");
        }

        invite.setStatus(InviteStatus.ACEITO);
        invite.setRespondedAt(LocalDateTime.now());

        FamilyGroup group = invite.getFamilyGroup();
        familyGroupMemberRepository.save(FamilyGroupMember.builder()
                .familyGroup(group)
                .user(user)
                .role(invite.getInvitedRole())
                .joinedAt(LocalDateTime.now())
                .build());

        return toGroupResponse(group, invite.getInvitedRole());
    }

    @Override
    public void declineInvite(Long inviteId) {
        String email = currentUserProvider.getCurrentUser().getEmail();
        FamilyInvite invite = findOwnInvite(inviteId, email);
        if (invite.getStatus() != InviteStatus.PENDENTE) {
            throw new BusinessException("Este convite já foi respondido");
        }
        invite.setStatus(InviteStatus.RECUSADO);
        invite.setRespondedAt(LocalDateTime.now());
    }

    @Override
    public FamilyGroupResponseDTO changeMemberRole(Long userId, ChangeMemberRoleRequestDTO dto) {
        if (dto.papel() == FamilyRole.OWNER) {
            throw new BusinessException("Não é possível transferir o papel de owner por aqui");
        }
        FamilyGroupMember myMembership = requireOwnerMembership();
        FamilyGroup group = myMembership.getFamilyGroup();

        FamilyGroupMember target = familyGroupMemberRepository.findByFamilyGroupIdAndUserId(group.getId(), userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Membro do grupo", userId));
        if (target.getRole() == FamilyRole.OWNER) {
            throw new BusinessException("Não é possível alterar o papel do owner do grupo");
        }

        target.setRole(dto.papel());
        return toGroupResponse(group, myMembership.getRole());
    }

    @Override
    public void removeMember(Long userId) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        FamilyGroupMember myMembership = familyGroupMemberRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new BusinessException("Você não pertence a nenhum grupo familiar"));
        FamilyGroup group = myMembership.getFamilyGroup();

        boolean removingSelf = userId.equals(currentUserId);
        if (removingSelf && myMembership.getRole() == FamilyRole.OWNER) {
            throw new BusinessException("O owner não pode sair do grupo — exclua o grupo em vez disso");
        }
        if (!removingSelf && myMembership.getRole() != FamilyRole.OWNER) {
            throw new AccessDeniedException("Apenas o owner pode remover outros membros");
        }

        FamilyGroupMember target = familyGroupMemberRepository.findByFamilyGroupIdAndUserId(group.getId(), userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Membro do grupo", userId));
        familyGroupMemberRepository.delete(target);
    }

    @Override
    public void deleteGroup() {
        FamilyGroupMember myMembership = requireOwnerMembership();
        familyGroupRepository.delete(myMembership.getFamilyGroup());
    }

    private FamilyGroupMember requireOwnerMembership() {
        Long userId = currentUserProvider.getCurrentUserId();
        FamilyGroupMember membership = familyGroupMemberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Você não pertence a nenhum grupo familiar"));
        if (membership.getRole() != FamilyRole.OWNER) {
            throw new AccessDeniedException("Apenas o owner do grupo pode realizar esta ação");
        }
        return membership;
    }

    private FamilyInvite findOwnInvite(Long inviteId, String email) {
        return familyInviteRepository.findByIdAndInvitedEmailIgnoreCase(inviteId, email)
                .orElseThrow(() -> ResourceNotFoundException.of("Convite", inviteId));
    }

    private FamilyGroupResponseDTO toGroupResponse(FamilyGroup group, FamilyRole currentUserRole) {
        List<FamilyGroupMemberResponseDTO> membros = familyGroupMemberRepository.findAllByFamilyGroupId(group.getId())
                .stream()
                .map(m -> new FamilyGroupMemberResponseDTO(
                        m.getUser().getId(), m.getUser().getName(), m.getUser().getEmail(), m.getRole()))
                .toList();

        return new FamilyGroupResponseDTO(
                group.getId(), group.getNome(), group.getOwnerUser().getId(), currentUserRole, membros);
    }

    private FamilyInviteResponseDTO toInviteResponse(FamilyInvite invite) {
        return new FamilyInviteResponseDTO(
                invite.getId(),
                invite.getFamilyGroup().getId(),
                invite.getFamilyGroup().getNome(),
                invite.getInvitedEmail(),
                invite.getInvitedRole(),
                invite.getStatus(),
                invite.getCreatedAt());
    }

}
