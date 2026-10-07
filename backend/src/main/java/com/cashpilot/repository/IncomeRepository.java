package com.cashpilot.repository;

import com.cashpilot.entity.Income;
import com.cashpilot.repository.projection.MovimentoDiario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

    Optional<Income> findByIdAndUserIdIn(Long id, List<Long> userIds);

    boolean existsByCategoriaId(Long categoriaId);

    boolean existsByContaBancariaId(Long contaId);

    /** Only received income counts, dated by when it was received (falls back to {@code data}). */
    @Query("""
            SELECT COALESCE(SUM(i.valor), 0) FROM Income i
            WHERE i.contaBancaria.id = :contaId AND i.recebida = true
              AND COALESCE(i.dataRecebimento, i.data) <= :ate
            """)
    BigDecimal sumRealizadoByContaBancariaIdAte(@Param("contaId") Long contaId, @Param("ate") LocalDate ate);

    @Query("""
            SELECT new com.cashpilot.repository.projection.MovimentoDiario(COALESCE(i.dataRecebimento, i.data), SUM(i.valor))
            FROM Income i
            WHERE i.contaBancaria.id = :contaId AND i.recebida = true
              AND COALESCE(i.dataRecebimento, i.data) BETWEEN :inicio AND :fim
            GROUP BY COALESCE(i.dataRecebimento, i.data)
            """)
    List<MovimentoDiario> sumRealizadoPorDia(@Param("contaId") Long contaId,
                                             @Param("inicio") LocalDate inicio,
                                             @Param("fim") LocalDate fim);

    @Query("""
            SELECT i FROM Income i
            WHERE i.user.id IN :userIds
              AND (CAST(:dataInicio AS date) IS NULL OR i.data >= :dataInicio)
              AND (CAST(:dataFim AS date) IS NULL OR i.data <= :dataFim)
              AND (CAST(:categoriaId AS long) IS NULL OR i.categoria.id = :categoriaId)
              AND (CAST(:contaId AS long) IS NULL OR i.contaBancaria.id = :contaId)
              AND (CAST(:recebida AS boolean) IS NULL OR i.recebida = :recebida)
            """)
    Page<Income> findAllByFilters(@Param("userIds") List<Long> userIds,
                                   @Param("dataInicio") LocalDate dataInicio,
                                   @Param("dataFim") LocalDate dataFim,
                                   @Param("categoriaId") Long categoriaId,
                                   @Param("contaId") Long contaId,
                                   @Param("recebida") Boolean recebida,
                                   Pageable pageable);

    @Query("""
            SELECT i FROM Income i
            LEFT JOIN FETCH i.categoria
            LEFT JOIN FETCH i.contaBancaria
            WHERE i.user.id IN :userIds
              AND (CAST(:dataInicio AS date) IS NULL OR i.data >= :dataInicio)
              AND (CAST(:dataFim AS date) IS NULL OR i.data <= :dataFim)
              AND (CAST(:categoriaId AS long) IS NULL OR i.categoria.id = :categoriaId)
              AND (CAST(:contaId AS long) IS NULL OR i.contaBancaria.id = :contaId)
              AND (CAST(:recebida AS boolean) IS NULL OR i.recebida = :recebida)
            """)
    List<Income> findAllByFiltersList(@Param("userIds") List<Long> userIds,
                                       @Param("dataInicio") LocalDate dataInicio,
                                       @Param("dataFim") LocalDate dataFim,
                                       @Param("categoriaId") Long categoriaId,
                                       @Param("contaId") Long contaId,
                                       @Param("recebida") Boolean recebida);

    /**
     * Income not yet in any realized balance as of {@code hoje} but expected by {@code dataFim}:
     * unreceived (including overdue), plus received-flagged entries dated in the future.
     */
    @Query("""
            SELECT i FROM Income i
            WHERE i.user.id IN :userIds
              AND (
                    (i.recebida = false AND i.data <= :dataFim)
                 OR (i.recebida = true
                     AND COALESCE(i.dataRecebimento, i.data) > :hoje
                     AND COALESCE(i.dataRecebimento, i.data) <= :dataFim)
                  )
            ORDER BY i.data ASC
            """)
    List<Income> findNaoRealizadasAte(@Param("userIds") List<Long> userIds,
                                      @Param("hoje") LocalDate hoje,
                                      @Param("dataFim") LocalDate dataFim);

    @Query("SELECT COALESCE(SUM(i.valor), 0) FROM Income i WHERE i.user.id IN :userIds AND i.data BETWEEN :dataInicio AND :dataFim")
    BigDecimal sumValorByUserIdAndDataBetween(@Param("userIds") List<Long> userIds,
                                               @Param("dataInicio") LocalDate dataInicio,
                                               @Param("dataFim") LocalDate dataFim);

}
