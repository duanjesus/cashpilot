package com.cashpilot.mapper;

import com.cashpilot.dto.response.IncomeResponseDTO;
import com.cashpilot.entity.Income;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IncomeMapper {

    @Mapping(source = "categoria.id", target = "categoriaId")
    @Mapping(source = "categoria.nome", target = "categoriaNome")
    @Mapping(source = "contaBancaria.id", target = "contaBancariaId")
    @Mapping(source = "contaBancaria.nome", target = "contaBancariaNome")
    IncomeResponseDTO toResponseDto(Income entity);

}
