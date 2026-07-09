package com.cashpilot.dto.response;

import java.util.List;

public record SimulacaoComparacaoResponseDTO(

        Integer horizonteMeses,

        List<CenarioSimulacaoResponseDTO> cenarios

) {
}
