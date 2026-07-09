package com.cashpilot.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UpdateGoalProgressRequestDTO(

        @NotNull(message = "O valor atual é obrigatório")
        @PositiveOrZero(message = "O valor atual deve ser maior ou igual a zero")
        BigDecimal valorAtual

) {
}
