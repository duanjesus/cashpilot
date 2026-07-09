package com.cashpilot.dto.response;

import com.cashpilot.entity.enums.CategoryType;

public record CategoryResponseDTO(
        Long id,
        String nome,
        CategoryType tipo,
        Boolean isSystem,
        Boolean isInvestment,
        String cor
) {
}
