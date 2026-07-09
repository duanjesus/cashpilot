package com.cashpilot.dto.request;

import com.cashpilot.entity.enums.GoalType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialGoalRequestDTO(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @NotNull(message = "O valor alvo é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor alvo deve ser maior que zero")
        BigDecimal valorAlvo,

        @NotNull(message = "A data alvo é obrigatória")
        LocalDate dataAlvo,

        BigDecimal valorAtual,

        Boolean ativa,

        /** Null defaults to {@code MANUAL}. */
        GoalType tipo,

        /** Null defaults to today. Only meaningful for {@code INVESTIMENTO} goals. */
        LocalDate dataInicio

) {
}
