package com.cashpilot.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponseDTO(
        BigDecimal saldoAtual,
        BigDecimal entradasMes,
        BigDecimal saidasMes,
        BigDecimal investimentosMes,
        MetaPrincipalResponseDTO metaPrincipal,
        List<ProximaContaResponseDTO> proximasContas
) {
}
