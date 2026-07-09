package com.cashpilot.service;

import com.cashpilot.dto.request.ExpenseRequestDTO;
import com.cashpilot.dto.request.MarkExpensePaidRequestDTO;
import com.cashpilot.dto.response.ExpenseResponseDTO;
import com.cashpilot.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {

    ExpenseResponseDTO create(ExpenseRequestDTO dto);

    ExpenseResponseDTO update(Long id, ExpenseRequestDTO dto);

    void delete(Long id);

    ExpenseResponseDTO findById(Long id);

    Page<ExpenseResponseDTO> findAll(LocalDate dataInicio, LocalDate dataFim, Long categoriaId, Long contaId,
                                      Long cartaoId, Boolean paga, Pageable pageable);

    ExpenseResponseDTO markAsPaid(Long id, MarkExpensePaidRequestDTO dto);

    /** Unpaginated, current-user-scoped fetch used by the Excel/PDF export endpoints. */
    List<Expense> findAllForExport(LocalDate dataInicio, LocalDate dataFim, Long categoriaId, Long contaId,
                                    Long cartaoId, Boolean paga);

}
