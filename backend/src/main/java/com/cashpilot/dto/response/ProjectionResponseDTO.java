package com.cashpilot.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectionResponseDTO(
        Boolean atingivel,
        BigDecimal aporteMensal,
        Integer mesesParaAtingir,
        Integer anos,
        Integer mesesRestantes,
        LocalDate dataEstimada,
        BigDecimal valorFinalProjetado,
        BigDecimal patrimonioAtual,
        BigDecimal valorAlvo,
        BigDecimal taxaRetornoMensal
) {
}
