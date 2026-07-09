package com.cashpilot.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProximaContaResponseDTO(
        Long id,
        String descricao,
        BigDecimal valor,
        LocalDate data,
        String categoriaNome,
        Long diasRestantes
) {
}
