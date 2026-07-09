package com.cashpilot.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record FluxoCaixaResponseDTO(
        BigDecimal saldoInicial,
        List<FluxoCaixaPontoDTO> serie,
        List<FluxoCaixaItemDTO> detalhamento
) {
}
