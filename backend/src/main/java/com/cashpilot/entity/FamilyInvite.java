package com.cashpilot.entity;

import com.cashpilot.entity.enums.FamilyRole;
import com.cashpilot.entity.enums.InviteStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "family_invites")
public class FamilyInvite extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_group_id", nullable = false)
    private FamilyGroup familyGroup;

    @Column(name = "invited_email", nullable = false, length = 150)
    private String invitedEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "invited_role", nullable = false, length = 20)
    private FamilyRole invitedRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InviteStatus status = InviteStatus.PENDENTE;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

}
