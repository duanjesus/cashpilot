package com.cashpilot.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/** {@code tipo} is one of {@code DESPESA_PENDENTE}, {@code RECEITA_PENDENTE}, {@code ASSINATURA_PROJETADA}. */
public record FluxoCaixaItemDTO(
        LocalDate data,
        String descricao,
        BigDecimal valor,
        String tipo,
        String origemNome
) {
}
