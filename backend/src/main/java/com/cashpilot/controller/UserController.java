package com.cashpilot.controller;

import com.cashpilot.dto.request.UpdateUserRequestDTO;
import com.cashpilot.dto.response.UserResponseDTO;
import com.cashpilot.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuário", description = "Perfil do usuário autenticado")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Buscar dados do usuário autenticado")
    public ResponseEntity<UserResponseDTO> me() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PutMapping("/me")
    @Operation(summary = "Atualizar dados do usuário autenticado")
    public ResponseEntity<UserResponseDTO> updateMe(@Valid @RequestBody UpdateUserRequestDTO dto) {
        return ResponseEntity.ok(userService.updateCurrentUser(dto));
    }

}
