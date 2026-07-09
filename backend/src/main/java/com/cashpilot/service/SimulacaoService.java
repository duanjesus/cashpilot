package com.cashpilot.service;

import com.cashpilot.dto.request.SimulacaoComparacaoRequestDTO;
import com.cashpilot.dto.response.SimulacaoComparacaoResponseDTO;

public interface SimulacaoService {

    /** Simulação puramente computacional — não lê nem grava dados do usuário. */
    SimulacaoComparacaoResponseDTO comparar(SimulacaoComparacaoRequestDTO dto);

}
