package com.cashpilot.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionRequestDTO(

        @NotNull(message = "A categoria é obrigatória")
        Long categoriaId,

        Long contaBancariaId,

        Long cartaoCreditoId,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres")
        String descricao,

        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        BigDecimal valor,

        @NotNull(message = "O dia de cobrança é obrigatório")
        @Min(value = 1, message = "O dia de cobrança deve ser entre 1 e 31")
        @Max(value = 31, message = "O dia de cobrança deve ser entre 1 e 31")
        Integer diaCobranca,

        @NotNull(message = "A data de início é obrigatória")
        LocalDate dataInicio,

        LocalDate dataFim,

        Boolean ativa,

        @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres")
        String observacoes

) {
}
