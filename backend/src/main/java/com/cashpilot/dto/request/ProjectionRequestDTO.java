package com.cashpilot.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProjectionRequestDTO(

        @NotNull(message = "O salário é obrigatório")
        @PositiveOrZero(message = "O salário deve ser maior ou igual a zero")
        BigDecimal salario,

        @NotNull(message = "As despesas fixas são obrigatórias")
        @PositiveOrZero(message = "As despesas fixas devem ser maiores ou iguais a zero")
        BigDecimal despesasFixas,

        @NotNull(message = "As despesas variáveis são obrigatórias")
        @PositiveOrZero(message = "As despesas variáveis devem ser maiores ou iguais a zero")
        BigDecimal despesasVariaveis,

        BigDecimal investimentoMensal,

        @NotNull(message = "O patrimônio atual é obrigatório")
        @PositiveOrZero(message = "O patrimônio atual deve ser maior ou igual a zero")
        BigDecimal patrimonioAtual,

        @NotNull(message = "O valor alvo é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor alvo deve ser maior que zero")
        BigDecimal valorAlvo,

        @NotNull(message = "A taxa de retorno mensal é obrigatória")
        @PositiveOrZero(message = "A taxa de retorno mensal deve ser maior ou igual a zero")
        BigDecimal taxaRetornoMensal

) {
}
