package com.cashpilot.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeResponseDTO(
        Long id,
        String descricao,
        BigDecimal valor,
        LocalDate data,
        Boolean recorrente,
        Boolean recebida,
        LocalDate dataRecebimento,
        String observacoes,
        Long categoriaId,
        String categoriaNome,
        Long contaBancariaId,
        String contaBancariaNome
) {
}
