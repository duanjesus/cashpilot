package com.cashpilot.service;

import com.cashpilot.dto.response.CapturaSaldoResponseDTO;
import com.cashpilot.dto.response.SaldoHistoricoPontoDTO;

import java.util.List;

public interface SaldoHistoricoService {

    /** Daily balance of one account for the last {@code dias} days, ending today. */
    List<SaldoHistoricoPontoDTO> getHistoricoConta(Long contaId, int dias);

    /** Daily balance summed across the caller's active accounts for the last {@code dias} days. */
    List<SaldoHistoricoPontoDTO> getHistoricoTotal(int dias);

    /** Writes any missing daily snapshots for the caller's accounts, same as the nightly job. */
    CapturaSaldoResponseDTO capturarPendentes();

}
