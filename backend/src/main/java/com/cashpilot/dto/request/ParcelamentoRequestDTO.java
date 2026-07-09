package com.cashpilot.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParcelamentoRequestDTO(

        @NotNull(message = "A categoria é obrigatória")
        Long categoriaId,

        Long contaBancariaId,

        Long cartaoCreditoId,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres")
        String descricao,

        @NotNull(message = "O valor total é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor total deve ser maior que zero")
        BigDecimal valorTotal,

        @NotNull(message = "O número de parcelas é obrigatório")
        @Min(value = 2, message = "O número de parcelas deve ser no mínimo 2")
        Integer numeroParcelas,

        @NotNull(message = "A data da primeira parcela é obrigatória")
        LocalDate dataPrimeiraParcela,

        @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres")
        String observacoes

) {
}
