package com.cashpilot.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CenarioSimulacaoResponseDTO(

        String nome,

        List<BigDecimal> pontos

) {
}
