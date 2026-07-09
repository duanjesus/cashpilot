package com.cashpilot.mapper;

import com.cashpilot.dto.response.BankAccountResponseDTO;
import com.cashpilot.entity.BankAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {

    @Mapping(target = "saldoAtual", ignore = true)
    BankAccountResponseDTO toResponseDto(BankAccount entity);

}
