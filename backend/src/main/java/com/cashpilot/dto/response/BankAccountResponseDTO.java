package com.cashpilot.dto.response;

import com.cashpilot.entity.enums.BankAccountType;
import com.cashpilot.entity.enums.ContaOrigem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BankAccountResponseDTO(
        Long id,
        String nome,
        String instituicao,
        BankAccountType tipo,
        BigDecimal saldoInicial,
        LocalDate dataSaldoInicial,
        Boolean ativa,
        BigDecimal saldoAtual,
        ContaOrigem origem,
        String instituicaoNome,
        LocalDateTime ultimaSincronizacao
) {
}
