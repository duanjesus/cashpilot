package com.cashpilot.dto.request;

import java.time.LocalDate;

/** {@code dataRecebimento} null means "today". */
public record MarkIncomeReceivedRequestDTO(
        LocalDate dataRecebimento
) {
}
