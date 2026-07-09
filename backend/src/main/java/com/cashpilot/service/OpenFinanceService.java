package com.cashpilot.service;

import com.cashpilot.dto.request.ConectarOpenFinanceRequestDTO;
import com.cashpilot.dto.response.BankAccountResponseDTO;
import com.cashpilot.dto.response.CreditCardResponseDTO;
import com.cashpilot.dto.response.InstituicaoMockDTO;

import java.util.List;

/**
 * Open Finance account-linking stub: demonstrates the data model/API shape for
 * a future real integration without ever calling a real bank API or fabricating
 * transaction data. "Sync" is a cosmetic timestamp bump only.
 */
public interface OpenFinanceService {

    /** Hardcoded, fully fictional list of mock institutions — no DB table behind this. */
    List<InstituicaoMockDTO> listarInstituicoes();

    BankAccountResponseDTO conectarConta(ConectarOpenFinanceRequestDTO dto);

    CreditCardResponseDTO conectarCartao(ConectarOpenFinanceRequestDTO dto);

    BankAccountResponseDTO sincronizarConta(Long id);

    CreditCardResponseDTO sincronizarCartao(Long id);

}
