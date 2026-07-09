package com.cashpilot.controller;

import com.cashpilot.dto.request.ChangeMemberRoleRequestDTO;
import com.cashpilot.dto.request.CreateFamilyGroupRequestDTO;
import com.cashpilot.dto.request.FamilyInviteRequestDTO;
import com.cashpilot.dto.response.FamilyGroupResponseDTO;
import com.cashpilot.dto.response.FamilyInviteResponseDTO;
import com.cashpilot.service.FamilyGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/grupos-familiares")
@RequiredArgsConstructor
@Tag(name = "Grupo Familiar", description = "Compartilhamento de dados financeiros entre múltiplos usuários")
public class FamilyGroupController {

    private final FamilyGroupService familyGroupService;

    @PostMapping
    @Operation(summary = "Criar grupo familiar")
    @ResponseStatus(HttpStatus.CREATED)
    public FamilyGroupResponseDTO create(@Valid @RequestBody CreateFamilyGroupRequestDTO dto) {
        return familyGroupService.create(dto);
    }

    @GetMapping("/me")
    @Operation(summary = "Obter o grupo familiar do usuário atual, se houver")
    public ResponseEntity<FamilyGroupResponseDTO> findMyGroup() {
        return familyGroupService.findMyGroup()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @DeleteMapping
    @Operation(summary = "Excluir o grupo familiar (apenas owner)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup() {
        familyGroupService.deleteGroup();
    }

    @PostMapping("/convites")
    @Operation(summary = "Convidar um usuário existente para o grupo (apenas owner)")
    @ResponseStatus(HttpStatus.CREATED)
    public FamilyInviteResponseDTO invite(@Valid @RequestBody FamilyInviteRequestDTO dto) {
        return familyGroupService.invite(dto);
    }

    @GetMapping("/convites/pendentes")
    @Operation(summary = "Listar convites pendentes endereçados ao usuário atual")
    public List<FamilyInviteResponseDTO> findPendingInvites() {
        return familyGroupService.findPendingInvitesForCurrentUser();
    }

    @PatchMapping("/convites/{id}/aceitar")
    @Operation(summary = "Aceitar um convite de grupo familiar")
    public FamilyGroupResponseDTO acceptInvite(@PathVariable Long id) {
        return familyGroupService.acceptInvite(id);
    }

    @PatchMapping("/convites/{id}/recusar")
    @Operation(summary = "Recusar um convite de grupo familiar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void declineInvite(@PathVariable Long id) {
        familyGroupService.declineInvite(id);
    }

    @PatchMapping("/membros/{userId}/papel")
    @Operation(summary = "Alterar o papel de um membro do grupo (apenas owner)")
    public FamilyGroupResponseDTO changeMemberRole(@PathVariable Long userId, @Valid @RequestBody ChangeMemberRoleRequestDTO dto) {
        return familyGroupService.changeMemberRole(userId, dto);
    }

    @DeleteMapping("/membros/{userId}")
    @Operation(summary = "Remover um membro do grupo, ou sair do grupo (o próprio usuário)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable Long userId) {
        familyGroupService.removeMember(userId);
    }

}
