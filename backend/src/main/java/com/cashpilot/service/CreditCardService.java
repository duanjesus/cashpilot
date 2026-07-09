package com.cashpilot.service;

import com.cashpilot.dto.request.CreditCardRequestDTO;
import com.cashpilot.dto.response.CreditCardResponseDTO;

import java.util.List;

public interface CreditCardService {

    CreditCardResponseDTO create(CreditCardRequestDTO dto);

    CreditCardResponseDTO update(Long id, CreditCardRequestDTO dto);

    void delete(Long id);

    CreditCardResponseDTO findById(Long id);

    List<CreditCardResponseDTO> findAll();

}
