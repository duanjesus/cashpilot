package com.cashpilot.dto.response;

import com.cashpilot.entity.enums.SaldoOrigem;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One day of balance history. {@code saldo} is recomputed from the transactions as they stand
 * now; {@code saldoRegistrado}/{@code origem} come from the stored snapshot and are absent when
 * the day has none (always the case for today). {@code divergente} flags a snapshot that no
 * longer matches the recomputed balance, i.e. a transaction for that day was changed afterwards.
 */
public record SaldoHistoricoPontoDTO(
        LocalDate data,
        BigDecimal saldo,
        BigDecimal saldoRegistrado,
        SaldoOrigem origem,
        boolean divergente
) {
}
