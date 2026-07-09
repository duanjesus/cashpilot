package com.cashpilot.service;

import com.cashpilot.dto.response.FluxoCaixaResponseDTO;

public interface FluxoCaixaService {

    /** {@code dias} null falls back to the configured default window. */
    FluxoCaixaResponseDTO getFluxoCaixa(Integer dias);

}
