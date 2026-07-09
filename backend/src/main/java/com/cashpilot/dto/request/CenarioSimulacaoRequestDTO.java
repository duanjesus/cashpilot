package com.cashpilot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CenarioSimulacaoRequestDTO(

        @NotBlank(message = "O nome do cenário é obrigatório")
        String nome,

        @NotNull(message = "O patrimônio inicial é obrigatório")
        @PositiveOrZero(message = "O patrimônio inicial deve ser maior ou igual a zero")
        BigDecimal patrimonioInicial,

        @NotNull(message = "O aporte mensal é obrigatório")
        @PositiveOrZero(message = "O aporte mensal deve ser maior ou igual a zero")
        BigDecimal aporteMensal,

        @NotNull(message = "A taxa de retorno mensal é obrigatória")
        @PositiveOrZero(message = "A taxa de retorno mensal deve ser maior ou igual a zero")
        BigDecimal taxaRetornoMensal

) {
}
