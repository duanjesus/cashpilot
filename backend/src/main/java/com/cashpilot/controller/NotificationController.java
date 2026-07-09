package com.cashpilot.controller;

import com.cashpilot.dto.response.NotificationResponseDTO;
import com.cashpilot.dto.response.UnreadCountResponseDTO;
import com.cashpilot.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notificacoes")
@RequiredArgsConstructor
@Tag(name = "Notificações", description = "Notificações in-app do usuário (contas a vencer, faturas fechando, metas atingidas)")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Listar notificações do usuário (paginado, filtro opcional por lida)")
    public ResponseEntity<Page<NotificationResponseDTO>> findAll(
            @RequestParam(required = false) Boolean lida,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(notificationService.findAll(lida, pageable));
    }

    @GetMapping("/nao-lidas/contagem")
    @Operation(summary = "Contar notificações não lidas do usuário")
    public ResponseEntity<UnreadCountResponseDTO> contarNaoLidas() {
        return ResponseEntity.ok(new UnreadCountResponseDTO(notificationService.countUnread()));
    }

    @PatchMapping("/{id}/marcar-lida")
    @Operation(summary = "Marcar notificação como lida")
    public ResponseEntity<NotificationResponseDTO> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PatchMapping("/marcar-todas-lidas")
    @Operation(summary = "Marcar todas as notificações do usuário como lidas")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir notificação")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        notificationService.delete(id);
    }

    @PostMapping("/gerar")
    @Operation(summary = "Gerar manualmente as notificações pendentes (contas a vencer, faturas fechando, metas atingidas)")
    public ResponseEntity<List<NotificationResponseDTO>> gerar() {
        return ResponseEntity.ok(notificationService.gerarPendentes());
    }

}
