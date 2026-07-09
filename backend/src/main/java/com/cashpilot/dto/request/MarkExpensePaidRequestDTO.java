package com.cashpilot.dto.request;

import java.time.LocalDate;

/** {@code dataPagamento} null means "today". */
public record MarkExpensePaidRequestDTO(
        LocalDate dataPagamento
) {
}
