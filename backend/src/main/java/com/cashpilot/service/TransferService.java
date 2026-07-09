package com.cashpilot.service;

import com.cashpilot.dto.request.TransferRequestDTO;
import com.cashpilot.dto.response.TransferResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransferService {

    TransferResponseDTO create(TransferRequestDTO dto);

    void delete(Long id);

    TransferResponseDTO findById(Long id);

    Page<TransferResponseDTO> findAll(Pageable pageable);

}
