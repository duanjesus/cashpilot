package com.cashpilot.service;

import com.cashpilot.dto.request.ParcelamentoRequestDTO;
import com.cashpilot.dto.response.ParcelamentoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ParcelamentoService {

    ParcelamentoResponseDTO create(ParcelamentoRequestDTO dto);

    void delete(Long id);

    ParcelamentoResponseDTO findById(Long id);

    Page<ParcelamentoResponseDTO> findAll(Pageable pageable);

}
