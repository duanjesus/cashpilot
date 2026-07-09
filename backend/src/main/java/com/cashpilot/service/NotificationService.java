package com.cashpilot.service;

import com.cashpilot.dto.response.NotificationResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    Page<NotificationResponseDTO> findAll(Boolean lida, Pageable pageable);

    long countUnread();

    NotificationResponseDTO markAsRead(Long id);

    void markAllAsRead();

    void delete(Long id);

    /** Generates any pending notifications system-wide, same logic the daily cron uses. */
    List<NotificationResponseDTO> gerarPendentes();

}
