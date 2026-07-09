package com.cashpilot.mapper;

import com.cashpilot.dto.response.TransferResponseDTO;
import com.cashpilot.entity.Transfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransferMapper {

    @Mapping(source = "contaOrigem.id", target = "contaOrigemId")
    @Mapping(source = "contaOrigem.nome", target = "contaOrigemNome")
    @Mapping(source = "contaDestino.id", target = "contaDestinoId")
    @Mapping(source = "contaDestino.nome", target = "contaDestinoNome")
    TransferResponseDTO toResponseDto(Transfer entity);

}
