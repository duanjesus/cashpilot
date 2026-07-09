package com.cashpilot.dto.response;

import com.cashpilot.entity.enums.CardBrand;

import java.math.BigDecimal;

public record CreditCardResponseDTO(
        Long id,
        String nome,
        CardBrand bandeira,
        BigDecimal limite,
        Integer diaFechamento,
        Integer diaVencimento,
        Long contaVinculadaId,
        String contaVinculadaNome,
        Boolean ativo,
        BigDecimal faturaAtual
) {
}
