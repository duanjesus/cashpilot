package com.cashpilot.service;

import com.cashpilot.dto.request.IncomeRequestDTO;
import com.cashpilot.dto.request.MarkIncomeReceivedRequestDTO;
import com.cashpilot.dto.response.IncomeResponseDTO;
import com.cashpilot.entity.Income;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface IncomeService {

    IncomeResponseDTO create(IncomeRequestDTO dto);

    IncomeResponseDTO update(Long id, IncomeRequestDTO dto);

    void delete(Long id);

    IncomeResponseDTO findById(Long id);

    Page<IncomeResponseDTO> findAll(LocalDate dataInicio, LocalDate dataFim, Long categoriaId, Long contaId, Boolean recebida, Pageable pageable);

    IncomeResponseDTO markAsReceived(Long id, MarkIncomeReceivedRequestDTO dto);

    /** Unpaginated, current-user-scoped fetch used by the Excel/PDF export endpoints. */
    List<Income> findAllForExport(LocalDate dataInicio, LocalDate dataFim, Long categoriaId, Long contaId, Boolean recebida);

}
