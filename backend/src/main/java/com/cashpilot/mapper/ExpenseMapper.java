package com.cashpilot.mapper;

import com.cashpilot.dto.response.ExpenseResponseDTO;
import com.cashpilot.entity.Expense;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(source = "categoria.id", target = "categoriaId")
    @Mapping(source = "categoria.nome", target = "categoriaNome")
    @Mapping(source = "contaBancaria.id", target = "contaBancariaId")
    @Mapping(source = "contaBancaria.nome", target = "contaBancariaNome")
    @Mapping(source = "cartaoCredito.id", target = "cartaoCreditoId")
    @Mapping(source = "cartaoCredito.nome", target = "cartaoCreditoNome")
    ExpenseResponseDTO toResponseDto(Expense entity);

}
