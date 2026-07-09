package com.cashpilot.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParcelamentoResponseDTO(
        Long id,
        String descricao,
        BigDecimal valorTotal,
        Integer numeroParcelas,
        LocalDate dataPrimeiraParcela,
        String observacoes,
        Long categoriaId,
        String categoriaNome,
        Long contaBancariaId,
        String contaBancariaNome,
        Long cartaoCreditoId,
        String cartaoCreditoNome,
        Integer parcelasPagas,
        BigDecimal valorPago,
        BigDecimal valorRestante,
        Boolean quitado
) {
}
