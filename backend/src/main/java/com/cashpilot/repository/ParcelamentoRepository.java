package com.cashpilot.repository;

import com.cashpilot.entity.Parcelamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParcelamentoRepository extends JpaRepository<Parcelamento, Long> {

    Optional<Parcelamento> findByIdAndUserId(Long id, Long userId);

    Page<Parcelamento> findAllByUserId(Long userId, Pageable pageable);

}
