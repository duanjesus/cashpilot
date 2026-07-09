package com.cashpilot.service;

import com.cashpilot.dto.response.DashboardSummaryResponseDTO;
import com.cashpilot.dto.response.EvolucaoSaldoPointDTO;

import java.util.List;

public interface DashboardService {

    DashboardSummaryResponseDTO getResumo();

    List<EvolucaoSaldoPointDTO> getEvolucaoSaldo(Integer dias);

}
