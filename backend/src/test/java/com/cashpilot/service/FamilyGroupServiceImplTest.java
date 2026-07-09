package com.cashpilot.service;

import com.cashpilot.dto.request.ChangeMemberRoleRequestDTO;
import com.cashpilot.dto.request.CreateFamilyGroupRequestDTO;
import com.cashpilot.dto.request.FamilyInviteRequestDTO;
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
import com.cashpilot.repository.FamilyGroupMemberRepository;
import com.cashpilot.repository.FamilyGroupRepository;
import com.cashpilot.repository.FamilyInviteRepository;
import com.cashpilot.repository.UserRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.impl.FamilyGroupServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FamilyGroupService")
class FamilyGroupServiceImplTest {

    @Mock
    private FamilyGroupRepository familyGroupRepository;

    @Mock
    private FamilyGroupMemberRepository familyGroupMemberRepository;

    @Mock
    private FamilyInviteRepository familyInviteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private FamilyGroupServiceImpl familyGroupService;

    private User owner;
    private User invited;
    private FamilyGroup group;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).name("Ana").email("ana@cashpilot.com").password("hash").build();
        invited = User.builder().id(2L).name("Bruno").email("bruno@cashpilot.com").password("hash").build();
        group = FamilyGroup.builder().id(100L).nome("Família Silva").ownerUser(owner).build();
    }

    @Test
    @DisplayName("Deve criar grupo familiar e tornar o criador owner")
    void deveCriarGrupoComSucesso() {
        when(currentUserProvider.getCurrentUser()).thenReturn(owner);
        when(familyGroupMemberRepository.existsByUserId(1L)).thenReturn(false);
        when(familyGroupRepository.save(any(FamilyGroup.class))).thenReturn(group);
        when(familyGroupMemberRepository.findAllByFamilyGroupId(100L)).thenReturn(List.of(
                FamilyGroupMember.builder().familyGroup(group).user(owner).role(FamilyRole.OWNER).build()));

        FamilyGroupResponseDTO response = familyGroupService.create(new CreateFamilyGroupRequestDTO("Família Silva"));

        assertThat(response.nome()).isEqualTo("Família Silva");
        assertThat(response.papelDoUsuarioAtual()).isEqualTo(FamilyRole.OWNER);
        assertThat(response.membros()).hasSize(1);
        verify(familyGroupMemberRepository).save(any(FamilyGroupMember.class));
    }

    @Test
    @DisplayName("Deve rejeitar criação de grupo se o usuário já pertence a um")
    void deveRejeitarCriacaoSeJaEstaEmGrupo() {
        when(currentUserProvider.getCurrentUser()).thenReturn(owner);
        when(familyGroupMemberRepository.existsByUserId(1L)).thenReturn(true);

        assertThatThrownBy(() -> familyGroupService.create(new CreateFamilyGroupRequestDTO("Outro grupo")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Deve convidar usuário existente com sucesso quando quem convida é owner")
    void deveConvidarComSucesso() {
        FamilyGroupMember ownerMembership = FamilyGroupMember.builder()
                .familyGroup(group).user(owner).role(FamilyRole.OWNER).build();
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(familyGroupMemberRepository.findByUserId(1L)).thenReturn(Optional.of(ownerMembership));
        when(userRepository.findByEmail("bruno@cashpilot.com")).thenReturn(Optional.of(invited));
        when(familyGroupMemberRepository.existsByUserId(2L)).thenReturn(false);
        when(familyInviteRepository.existsByFamilyGroupIdAndInvitedEmailIgnoreCaseAndStatus(100L, "bruno@cashpilot.com", InviteStatus.PENDENTE))
                .thenReturn(false);
        when(familyInviteRepository.save(any(FamilyInvite.class))).thenAnswer(inv -> {
            FamilyInvite invite = inv.getArgument(0);
            invite.setId(500L);
            return invite;
        });

        FamilyInviteResponseDTO response = familyGroupService.invite(
                new FamilyInviteRequestDTO("bruno@cashpilot.com", FamilyRole.MEMBER));

        assertThat(response.invitedEmail()).isEqualTo("bruno@cashpilot.com");
        assertThat(response.invitedRole()).isEqualTo(FamilyRole.MEMBER);
        assertThat(response.status()).isEqualTo(InviteStatus.PENDENTE);
    }

    @Test
    @DisplayName("Deve rejeitar convite de quem não é owner do grupo")
    void deveRejeitarConviteDeNaoOwner() {
        FamilyGroupMember memberMembership = FamilyGroupMember.builder()
                .familyGroup(group).user(invited).role(FamilyRole.MEMBER).build();
        when(currentUserProvider.getCurrentUserId()).thenReturn(2L);
        when(familyGroupMemberRepository.findByUserId(2L)).thenReturn(Optional.of(memberMembership));

        assertThatThrownBy(() -> familyGroupService.invite(
                new FamilyInviteRequestDTO("carla@cashpilot.com", FamilyRole.MEMBER)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Deve rejeitar convite para e-mail sem usuário cadastrado")
    void deveRejeitarConviteParaEmailInexistente() {
        FamilyGroupMember ownerMembership = FamilyGroupMember.builder()
                .familyGroup(group).user(owner).role(FamilyRole.OWNER).build();
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(familyGroupMemberRepository.findByUserId(1L)).thenReturn(Optional.of(ownerMembership));
        when(userRepository.findByEmail("naoexiste@cashpilot.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> familyGroupService.invite(
                new FamilyInviteRequestDTO("naoexiste@cashpilot.com", FamilyRole.MEMBER)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Deve aceitar convite pendente e criar a membership")
    void deveAceitarConviteComSucesso() {
        FamilyInvite invite = FamilyInvite.builder()
                .id(500L).familyGroup(group).invitedEmail("bruno@cashpilot.com")
                .invitedRole(FamilyRole.MEMBER).status(InviteStatus.PENDENTE).build();

        when(currentUserProvider.getCurrentUser()).thenReturn(invited);
        when(familyGroupMemberRepository.existsByUserId(2L)).thenReturn(false);
        when(familyInviteRepository.findByIdAndInvitedEmailIgnoreCase(500L, "bruno@cashpilot.com"))
                .thenReturn(Optional.of(invite));
        when(familyGroupMemberRepository.findAllByFamilyGroupId(100L)).thenReturn(List.of(
                FamilyGroupMember.builder().familyGroup(group).user(owner).role(FamilyRole.OWNER).build(),
                FamilyGroupMember.builder().familyGroup(group).user(invited).role(FamilyRole.MEMBER).build()));

        FamilyGroupResponseDTO response = familyGroupService.acceptInvite(500L);

        assertThat(invite.getStatus()).isEqualTo(InviteStatus.ACEITO);
        assertThat(response.membros()).hasSize(2);
        verify(familyGroupMemberRepository).save(any(FamilyGroupMember.class));
    }

    @Test
    @DisplayName("Owner não pode sair do grupo (deve excluir o grupo em vez disso)")
    void ownerNaoPodeSairDoGrupo() {
        FamilyGroupMember ownerMembership = FamilyGroupMember.builder()
                .familyGroup(group).user(owner).role(FamilyRole.OWNER).build();
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(familyGroupMemberRepository.findByUserId(1L)).thenReturn(Optional.of(ownerMembership));

        assertThatThrownBy(() -> familyGroupService.removeMember(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Deve rejeitar troca de papel para OWNER via changeMemberRole")
    void deveRejeitarTrocaDePapelParaOwner() {
        assertThatThrownBy(() -> familyGroupService.changeMemberRole(2L, new ChangeMemberRoleRequestDTO(FamilyRole.OWNER)))
                .isInstanceOf(BusinessException.class);
    }

}
