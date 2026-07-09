package com.cashpilot.dto.response;

import java.math.BigDecimal;

public record RelatorioMensalDTO(

        String anoMes,

        BigDecimal entradas,

        BigDecimal saidas,

        BigDecimal investimentos,

        BigDecimal saldoLiquido

) {
}
