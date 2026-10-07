package com.cashpilot.entity.enums;

/** How a daily balance snapshot came to exist. */
public enum SaldoOrigem {
    /** Recorded right after the day closed. */
    CAPTURADO,
    /** Back-filled later from the transactions dated on or before that day. */
    RECONSTRUIDO
}
