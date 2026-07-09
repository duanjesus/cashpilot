package com.cashpilot.mapper;

import com.cashpilot.dto.response.FinancialGoalResponseDTO;
import com.cashpilot.entity.FinancialGoal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FinancialGoalMapper {

    @Mapping(target = "progresso", ignore = true)
    FinancialGoalResponseDTO toResponseDto(FinancialGoal entity);

}
