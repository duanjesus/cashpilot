package com.cashpilot.mapper;

import com.cashpilot.dto.response.ParcelamentoResponseDTO;
import com.cashpilot.entity.Parcelamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ParcelamentoMapper {

    @Mapping(source = "categoria.id", target = "categoriaId")
    @Mapping(source = "categoria.nome", target = "categoriaNome")
    @Mapping(source = "contaBancaria.id", target = "contaBancariaId")
    @Mapping(source = "contaBancaria.nome", target = "contaBancariaNome")
    @Mapping(source = "cartaoCredito.id", target = "cartaoCreditoId")
    @Mapping(source = "cartaoCredito.nome", target = "cartaoCreditoNome")
    @Mapping(target = "parcelasPagas", ignore = true)
    @Mapping(target = "valorPago", ignore = true)
    @Mapping(target = "valorRestante", ignore = true)
    @Mapping(target = "quitado", ignore = true)
    ParcelamentoResponseDTO toResponseDto(Parcelamento entity);

}
