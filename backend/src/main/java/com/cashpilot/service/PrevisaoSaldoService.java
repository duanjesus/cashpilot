package com.cashpilot.service;

import com.cashpilot.dto.response.PrevisaoSaldoResponseDTO;

public interface PrevisaoSaldoService {

    /**
     * Projeta o saldo futuro com base na média mensal histórica de saldo líquido.
     *
     * @param mesesHistorico quantos meses de histórico considerar na média (padrão 6 quando nulo)
     * @param mesesProjecao  quantos meses futuros projetar (padrão 12 quando nulo)
     */
    PrevisaoSaldoResponseDTO getPrevisao(Integer mesesHistorico, Integer mesesProjecao);

}
