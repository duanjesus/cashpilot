package com.cashpilot.service;

import com.cashpilot.dto.request.BankAccountRequestDTO;
import com.cashpilot.dto.response.BankAccountResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface BankAccountService {

    BankAccountResponseDTO create(BankAccountRequestDTO dto);

    BankAccountResponseDTO update(Long id, BankAccountRequestDTO dto);

    void delete(Long id);

    BankAccountResponseDTO findById(Long id);

    List<BankAccountResponseDTO> findAll();

    /** Sum of today's realized balance across the current user's active accounts. */
    BigDecimal getSaldoAtualTotal();

}
