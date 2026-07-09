package com.cashpilot.dto.response;

import java.math.BigDecimal;

public record ProjectionPreferenceResponseDTO(
        BigDecimal salario,
        BigDecimal despesasFixas,
        BigDecimal despesasVariaveis,
        BigDecimal investimentoMensal,
        BigDecimal patrimonioAtual,
        BigDecimal valorAlvo,
        BigDecimal taxaRetornoMensal
) {
}
