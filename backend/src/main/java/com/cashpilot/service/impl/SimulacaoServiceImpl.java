package com.cashpilot.service.impl;

import com.cashpilot.dto.request.CenarioSimulacaoRequestDTO;
import com.cashpilot.dto.request.SimulacaoComparacaoRequestDTO;
import com.cashpilot.dto.response.CenarioSimulacaoResponseDTO;
import com.cashpilot.dto.response.SimulacaoComparacaoResponseDTO;
import com.cashpilot.service.ProjectionCalculator;
import com.cashpilot.service.SimulacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimulacaoServiceImpl implements SimulacaoService {

    private final ProjectionCalculator projectionCalculator;

    @Override
    public SimulacaoComparacaoResponseDTO comparar(SimulacaoComparacaoRequestDTO dto) {
        List<CenarioSimulacaoResponseDTO> cenarios = dto.cenarios().stream()
                .map(cenario -> toResponse(cenario, dto.horizonteMeses()))
                .toList();
        return new SimulacaoComparacaoResponseDTO(dto.horizonteMeses(), cenarios);
    }

    private CenarioSimulacaoResponseDTO toResponse(CenarioSimulacaoRequestDTO cenario, int horizonteMeses) {
        List<BigDecimal> pontos = projectionCalculator.simularEvolucaoMensal(
                cenario.patrimonioInicial(), cenario.aporteMensal(), cenario.taxaRetornoMensal(), horizonteMeses);
        return new CenarioSimulacaoResponseDTO(cenario.nome(), pontos);
    }

}
