package com.cashpilot.service;

import com.cashpilot.dto.request.CategoryRequestDTO;
import com.cashpilot.dto.response.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {

    CategoryResponseDTO create(CategoryRequestDTO dto);

    CategoryResponseDTO update(Long id, CategoryRequestDTO dto);

    void delete(Long id);

    CategoryResponseDTO findById(Long id);

    List<CategoryResponseDTO> findAllVisible();

}
