package com.cashpilot.mapper;

import com.cashpilot.dto.response.SubscriptionResponseDTO;
import com.cashpilot.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(source = "categoria.id", target = "categoriaId")
    @Mapping(source = "categoria.nome", target = "categoriaNome")
    @Mapping(source = "contaBancaria.id", target = "contaBancariaId")
    @Mapping(source = "contaBancaria.nome", target = "contaBancariaNome")
    @Mapping(source = "cartaoCredito.id", target = "cartaoCreditoId")
    @Mapping(source = "cartaoCredito.nome", target = "cartaoCreditoNome")
    SubscriptionResponseDTO toResponseDto(Subscription entity);

}
