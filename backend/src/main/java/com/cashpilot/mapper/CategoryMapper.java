package com.cashpilot.mapper;

import com.cashpilot.dto.response.CategoryResponseDTO;
import com.cashpilot.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponseDTO toResponseDto(Category entity);

}
