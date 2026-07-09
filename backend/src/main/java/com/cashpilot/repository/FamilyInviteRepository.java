package com.cashpilot.repository;

import com.cashpilot.entity.FamilyInvite;
import com.cashpilot.entity.enums.InviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyInviteRepository extends JpaRepository<FamilyInvite, Long> {

    List<FamilyInvite> findAllByInvitedEmailIgnoreCaseAndStatus(String invitedEmail, InviteStatus status);

    boolean existsByFamilyGroupIdAndInvitedEmailIgnoreCaseAndStatus(Long familyGroupId, String invitedEmail, InviteStatus status);

    Optional<FamilyInvite> findByIdAndInvitedEmailIgnoreCase(Long id, String invitedEmail);

    List<FamilyInvite> findAllByFamilyGroupId(Long familyGroupId);

}
