package com.cashpilot.repository;

import com.cashpilot.entity.Notification;
import com.cashpilot.entity.enums.NotificationTipo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Notification> findAllByUserIdAndLidaOrderByCreatedAtDesc(Long userId, Boolean lida, Pageable pageable);

    long countByUserIdAndLidaFalse(Long userId);

    boolean existsByUserIdAndTipoAndReferenciaTipoAndReferenciaIdAndAnoMesReferencia(
            Long userId, NotificationTipo tipo, String referenciaTipo, Long referenciaId, String anoMesReferencia);

    List<Notification> findAllByUserId(Long userId);

}
