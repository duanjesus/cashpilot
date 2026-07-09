package com.cashpilot.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EvolucaoSaldoPointDTO(
        LocalDate data,
        BigDecimal valorAcumulado
) {
}
