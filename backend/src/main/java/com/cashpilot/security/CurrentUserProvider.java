package com.cashpilot.security;

import com.cashpilot.entity.FamilyGroupMember;
import com.cashpilot.entity.User;
import com.cashpilot.entity.enums.FamilyRole;
import com.cashpilot.exception.ResourceNotFoundException;
import com.cashpilot.repository.FamilyGroupMemberRepository;
import com.cashpilot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Resolves the {@link User} entity for the currently authenticated principal.
 * Every service that owns per-user data calls this to scope its queries.
 */
@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;
    private final FamilyGroupMemberRepository familyGroupMemberRepository;

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Ids of every user whose data the caller may see/edit: just themselves if solo,
     * or every member of their family group if they belong to one. Ownership of a row
     * (the FK on the entity) never changes — only this read-time scope resolution does.
     */
    public List<Long> getScopeUserIds() {
        return resolveScopeUserIds(getCurrentUserId());
    }

    /**
     * Same resolution as {@link #getScopeUserIds()} but for an explicit user id instead of
     * the request's authenticated principal — safe to call from background jobs that have
     * no {@link SecurityContextHolder} context (e.g. {@code NotificationSchedulerJob}).
     */
    public List<Long> resolveScopeUserIds(Long userId) {
        return familyGroupMemberRepository.findByUserId(userId)
                .map(membership -> familyGroupMemberRepository
                        .findAllByFamilyGroupId(membership.getFamilyGroup().getId())
                        .stream()
                        .map(m -> m.getUser().getId())
                        .toList())
                .orElse(List.of(userId));
    }

    public FamilyRole getCurrentRole() {
        return familyGroupMemberRepository.findByUserId(getCurrentUserId())
                .map(FamilyGroupMember::getRole)
                .orElse(FamilyRole.OWNER);
    }

    public void requireWriteAccess() {
        if (getCurrentRole() == FamilyRole.VIEWER) {
            throw new AccessDeniedException("Usuários com acesso somente leitura não podem realizar esta ação");
        }
    }

}
