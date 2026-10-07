package com.cashpilot.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Total moved on one bank account on one day, as returned by the per-day aggregate queries. */
public record MovimentoDiario(LocalDate dia, BigDecimal total) {
}
