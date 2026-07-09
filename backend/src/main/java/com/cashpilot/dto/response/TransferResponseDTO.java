package com.cashpilot.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransferResponseDTO(
        Long id,
        BigDecimal valor,
        LocalDate data,
        String descricao,
        Long contaOrigemId,
        String contaOrigemNome,
        Long contaDestinoId,
        String contaDestinoNome
) {
}
