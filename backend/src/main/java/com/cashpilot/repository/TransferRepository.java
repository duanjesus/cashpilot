package com.cashpilot.repository;

import com.cashpilot.entity.Transfer;
import com.cashpilot.repository.projection.MovimentoDiario;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    Optional<Transfer> findByIdAndUserIdIn(Long id, List<Long> userIds);

    Page<Transfer> findAllByUserIdIn(List<Long> userIds, Pageable pageable);

    boolean existsByContaOrigemId(Long contaId);

    boolean existsByContaDestinoId(Long contaId);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transfer t WHERE t.contaOrigem.id = :contaId AND t.data <= :ate")
    BigDecimal sumValorByContaOrigemIdAte(@Param("contaId") Long contaId, @Param("ate") LocalDate ate);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transfer t WHERE t.contaDestino.id = :contaId AND t.data <= :ate")
    BigDecimal sumValorByContaDestinoIdAte(@Param("contaId") Long contaId, @Param("ate") LocalDate ate);

    @Query("""
            SELECT new com.cashpilot.repository.projection.MovimentoDiario(t.data, SUM(t.valor))
            FROM Transfer t
            WHERE t.contaOrigem.id = :contaId AND t.data BETWEEN :inicio AND :fim
            GROUP BY t.data
            """)
    List<MovimentoDiario> sumSaidasPorDia(@Param("contaId") Long contaId,
                                          @Param("inicio") LocalDate inicio,
                                          @Param("fim") LocalDate fim);

    @Query("""
            SELECT new com.cashpilot.repository.projection.MovimentoDiario(t.data, SUM(t.valor))
            FROM Transfer t
            WHERE t.contaDestino.id = :contaId AND t.data BETWEEN :inicio AND :fim
            GROUP BY t.data
            """)
    List<MovimentoDiario> sumEntradasPorDia(@Param("contaId") Long contaId,
                                            @Param("inicio") LocalDate inicio,
                                            @Param("fim") LocalDate fim);

}
