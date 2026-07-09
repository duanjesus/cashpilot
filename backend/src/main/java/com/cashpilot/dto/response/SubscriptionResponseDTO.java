package com.cashpilot.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionResponseDTO(
        Long id,
        String descricao,
        BigDecimal valor,
        Integer diaCobranca,
        LocalDate dataInicio,
        LocalDate dataFim,
        Boolean ativa,
        String observacoes,
        Long categoriaId,
        String categoriaNome,
        Long contaBancariaId,
        String contaBancariaNome,
        Long cartaoCreditoId,
        String cartaoCreditoNome
) {
}
