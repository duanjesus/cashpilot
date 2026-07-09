package com.cashpilot.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FluxoCaixaPontoDTO(
        LocalDate data,
        BigDecimal saldoProjetado
) {
}
