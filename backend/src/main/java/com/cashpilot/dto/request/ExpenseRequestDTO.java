package com.cashpilot.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequestDTO(

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

        @NotNull(message = "A data é obrigatória")
        LocalDate data,

        Boolean paga,

        LocalDate dataPagamento,

        @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres")
        String observacoes

) {
}
