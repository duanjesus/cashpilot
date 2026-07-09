package com.cashpilot.repository;

import com.cashpilot.entity.Parcelamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParcelamentoRepository extends JpaRepository<Parcelamento, Long> {

    Optional<Parcelamento> findByIdAndUserIdIn(Long id, List<Long> userIds);

    Page<Parcelamento> findAllByUserIdIn(List<Long> userIds, Pageable pageable);

}
