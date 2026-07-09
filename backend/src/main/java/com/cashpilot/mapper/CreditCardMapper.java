package com.cashpilot.mapper;

import com.cashpilot.dto.response.CreditCardResponseDTO;
import com.cashpilot.entity.CreditCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreditCardMapper {

    @Mapping(source = "contaVinculada.id", target = "contaVinculadaId")
    @Mapping(source = "contaVinculada.nome", target = "contaVinculadaNome")
    @Mapping(target = "faturaAtual", ignore = true)
    CreditCardResponseDTO toResponseDto(CreditCard entity);

}
