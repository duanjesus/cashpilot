package com.cashpilot.service;

import com.cashpilot.dto.request.SubscriptionRequestDTO;
import com.cashpilot.dto.response.ExpenseResponseDTO;
import com.cashpilot.dto.response.SubscriptionResponseDTO;

import java.util.List;

public interface SubscriptionService {

    SubscriptionResponseDTO create(SubscriptionRequestDTO dto);

    SubscriptionResponseDTO update(Long id, SubscriptionRequestDTO dto);

    void delete(Long id);

    SubscriptionResponseDTO findById(Long id);

    List<SubscriptionResponseDTO> findAll();

    /** Generates any pending charges for the current user's active subscriptions, up to today. */
    List<ExpenseResponseDTO> gerarPendentes();

}
