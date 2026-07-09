package com.cashpilot.dto.response;

import com.cashpilot.entity.enums.GoalType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialGoalResponseDTO(
        Long id,
        String nome,
        BigDecimal valorAlvo,
        LocalDate dataAlvo,
        BigDecimal valorAtual,
        Boolean ativa,
        BigDecimal progresso,
        GoalType tipo,
        LocalDate dataInicio
) {
}
