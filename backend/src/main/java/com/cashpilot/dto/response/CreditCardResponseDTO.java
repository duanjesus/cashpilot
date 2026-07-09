package com.cashpilot.dto.response;

import com.cashpilot.entity.enums.CardBrand;
import com.cashpilot.entity.enums.ContaOrigem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        BigDecimal faturaAtual,
        ContaOrigem origem,
        String instituicaoNome,
        LocalDateTime ultimaSincronizacao
) {
}
