package com.cashpilot.dto.request;

import com.cashpilot.entity.enums.BankAccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BankAccountRequestDTO(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @Size(max = 100, message = "A instituição deve ter no máximo 100 caracteres")
        String instituicao,

        @NotNull(message = "O tipo é obrigatório")
        BankAccountType tipo,

        @NotNull(message = "O saldo inicial é obrigatório")
        BigDecimal saldoInicial,

        @NotNull(message = "A data do saldo inicial é obrigatória")
        LocalDate dataSaldoInicial,

        Boolean ativa

) {
}
