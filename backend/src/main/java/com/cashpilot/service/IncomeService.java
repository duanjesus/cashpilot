package com.cashpilot.service;

import com.cashpilot.dto.request.IncomeRequestDTO;
import com.cashpilot.dto.response.IncomeResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface IncomeService {

    IncomeResponseDTO create(IncomeRequestDTO dto);

    IncomeResponseDTO update(Long id, IncomeRequestDTO dto);

    void delete(Long id);

    IncomeResponseDTO findById(Long id);

    Page<IncomeResponseDTO> findAll(LocalDate dataInicio, LocalDate dataFim, Long categoriaId, Long contaId, Pageable pageable);

}
