package com.cashpilot.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponseDTO(
        Long id,
        String descricao,
        BigDecimal valor,
        LocalDate data,
        Boolean paga,
        LocalDate dataPagamento,
        String observacoes,
        Long categoriaId,
        String categoriaNome,
        Long contaBancariaId,
        String contaBancariaNome,
        Long cartaoCreditoId,
        String cartaoCreditoNome
) {
}
