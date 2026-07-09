package com.cashpilot.service;

import com.cashpilot.dto.response.RelatorioMensalDTO;

import java.util.List;

public interface RelatorioService {

    /** Relatório mensal (entradas, saídas, investimentos, saldo líquido) dos últimos {@code meses}, ordenado do mais antigo para o mais recente. */
    List<RelatorioMensalDTO> getRelatorioMensal(int meses);

}
