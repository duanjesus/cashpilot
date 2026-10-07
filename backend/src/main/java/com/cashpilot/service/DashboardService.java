package com.cashpilot.service;

import com.cashpilot.dto.response.DashboardSummaryResponseDTO;
import com.cashpilot.dto.response.SaldoHistoricoPontoDTO;

import java.util.List;

public interface DashboardService {

    DashboardSummaryResponseDTO getResumo();

    List<SaldoHistoricoPontoDTO> getEvolucaoSaldo(Integer dias);

}
