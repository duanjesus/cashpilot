package com.cashpilot.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record PrevisaoSaldoResponseDTO(

        BigDecimal saldoAtual,

        BigDecimal mediaMensalHistorica,

        List<PrevisaoSaldoPontoDTO> serie

) {
}
