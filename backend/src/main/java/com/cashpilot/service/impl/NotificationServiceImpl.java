package com.cashpilot.service.impl;

import com.cashpilot.dto.response.NotificationResponseDTO;
import com.cashpilot.entity.Notification;
import com.cashpilot.exception.ResourceNotFoundException;
import com.cashpilot.repository.NotificationRepository;
import com.cashpilot.scheduler.NotificationSchedulerJob;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSchedulerJob notificationSchedulerJob;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> findAll(Boolean lida, Pageable pageable) {
        Long userId = currentUserProvider.getCurrentUserId();
        Page<Notification> page = lida == null
                ? notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable)
                : notificationRepository.findAllByUserIdAndLidaOrderByCreatedAtDesc(userId, lida, pageable);
        return page.map(this::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread() {
        return notificationRepository.countByUserIdAndLidaFalse(currentUserProvider.getCurrentUserId());
    }

    @Override
    public NotificationResponseDTO markAsRead(Long id) {
        Notification notification = findOwnedEntityById(id);
        notification.setLida(true);
        Notification updated = notificationRepository.save(notification);
        return toResponseDto(updated);
    }

    @Override
    public void markAllAsRead() {
        Long userId = currentUserProvider.getCurrentUserId();
        List<Notification> notificacoes = notificationRepository.findAllByUserId(userId);
        for (Notification notification : notificacoes) {
            notification.setLida(true);
        }
        notificationRepository.saveAll(notificacoes);
    }

    @Override
    public void delete(Long id) {
        Notification notification = findOwnedEntityById(id);
        notificationRepository.delete(notification);
    }

    @Override
    public List<NotificationResponseDTO> gerarPendentes() {
        return notificationSchedulerJob.gerarNotificacoesPendentes().stream()
                .map(this::toResponseDto)
                .toList();
    }

    private Notification findOwnedEntityById(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Notificação", id));
        if (!notification.getUser().getId().equals(userId)) {
            throw ResourceNotFoundException.of("Notificação", id);
        }
        return notification;
    }

    private NotificationResponseDTO toResponseDto(Notification notification) {
        return new NotificationResponseDTO(
                notification.getId(),
                notification.getTipo(),
                notification.getMensagem(),
                notification.getReferenciaTipo(),
                notification.getReferenciaId(),
                notification.getLida(),
                notification.getCreatedAt()
        );
    }

}
