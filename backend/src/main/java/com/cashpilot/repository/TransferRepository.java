package com.cashpilot.repository;

import com.cashpilot.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    Optional<Transfer> findByIdAndUserId(Long id, Long userId);

    Page<Transfer> findAllByUserId(Long userId, Pageable pageable);

    boolean existsByContaOrigemId(Long contaId);

    boolean existsByContaDestinoId(Long contaId);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transfer t WHERE t.contaOrigem.id = :contaId")
    BigDecimal sumValorByContaOrigemId(@Param("contaId") Long contaId);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transfer t WHERE t.contaDestino.id = :contaId")
    BigDecimal sumValorByContaDestinoId(@Param("contaId") Long contaId);

}
