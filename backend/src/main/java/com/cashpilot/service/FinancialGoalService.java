package com.cashpilot.service;

import com.cashpilot.dto.request.FinancialGoalRequestDTO;
import com.cashpilot.dto.request.UpdateGoalProgressRequestDTO;
import com.cashpilot.dto.response.FinancialGoalResponseDTO;

import java.util.List;

public interface FinancialGoalService {

    FinancialGoalResponseDTO create(FinancialGoalRequestDTO dto);

    FinancialGoalResponseDTO update(Long id, FinancialGoalRequestDTO dto);

    void delete(Long id);

    FinancialGoalResponseDTO findById(Long id);

    List<FinancialGoalResponseDTO> findAll();

    FinancialGoalResponseDTO updateProgress(Long id, UpdateGoalProgressRequestDTO dto);

}
