package com.cashpilot.service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Output of {@link ProjectionCalculator#calculate}. Plain carrier, no persistence concerns.
 */
public record ProjectionResult(
        boolean atingivel,
        Integer mesesParaAtingir,
        Integer anos,
        Integer mesesRestantes,
        LocalDate dataEstimada,
        BigDecimal valorFinalProjetado,
        BigDecimal aporteMensal
) {
}
