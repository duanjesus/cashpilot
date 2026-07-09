package com.cashpilot.dto.response;

import com.cashpilot.entity.enums.NotificationTipo;

import java.time.LocalDateTime;

public record NotificationResponseDTO(
        Long id,
        NotificationTipo tipo,
        String mensagem,
        String referenciaTipo,
        Long referenciaId,
        Boolean lida,
        LocalDateTime createdAt
) {
}
