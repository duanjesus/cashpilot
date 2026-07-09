package com.cashpilot.dto.response;

import java.math.BigDecimal;

public record PrevisaoSaldoPontoDTO(

        String anoMes,

        BigDecimal saldoProjetado

) {
}
