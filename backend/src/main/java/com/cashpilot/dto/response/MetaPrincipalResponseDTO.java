package com.cashpilot.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MetaPrincipalResponseDTO(
        Long id,
        String nome,
        BigDecimal valorAlvo,
        BigDecimal valorAtual,
        BigDecimal progresso,
        LocalDate dataAlvo
) {
}
