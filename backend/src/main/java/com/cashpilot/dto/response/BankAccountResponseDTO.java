package com.cashpilot.dto.response;

import com.cashpilot.entity.enums.BankAccountType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BankAccountResponseDTO(
        Long id,
        String nome,
        String instituicao,
        BankAccountType tipo,
        BigDecimal saldoInicial,
        LocalDate dataSaldoInicial,
        Boolean ativa,
        BigDecimal saldoAtual
) {
}
