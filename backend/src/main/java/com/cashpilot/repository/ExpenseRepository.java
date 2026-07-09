package com.cashpilot.repository;

import com.cashpilot.entity.Expense;
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
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Optional<Expense> findByIdAndUserIdIn(Long id, List<Long> userIds);

    boolean existsByCategoriaId(Long categoriaId);

    boolean existsByContaBancariaId(Long contaId);

    boolean existsByCartaoCreditoId(Long cartaoId);

    boolean existsByParcelamentoId(Long parcelamentoId);

    boolean existsByParcelamentoIdAndPagaTrue(Long parcelamentoId);

    List<Expense> findAllByParcelamentoId(Long parcelamentoId);

    boolean existsByAssinaturaIdAndReferenciaMes(Long assinaturaId, LocalDate referenciaMes);

    @Query("SELECT COALESCE(SUM(e.valor), 0) FROM Expense e WHERE e.contaBancaria.id = :contaId")
    BigDecimal sumValorByContaBancariaId(@Param("contaId") Long contaId);

    @Query("SELECT COALESCE(SUM(e.valor), 0) FROM Expense e WHERE e.cartaoCredito.id = :cartaoId AND e.paga = false")
    BigDecimal sumValorByCartaoCreditoIdAndPagaFalse(@Param("cartaoId") Long cartaoId);

    @Query("""
            SELECT e FROM Expense e
            WHERE e.user.id IN :userIds
              AND (CAST(:dataInicio AS date) IS NULL OR e.data >= :dataInicio)
              AND (CAST(:dataFim AS date) IS NULL OR e.data <= :dataFim)
              AND (CAST(:categoriaId AS long) IS NULL OR e.categoria.id = :categoriaId)
              AND (CAST(:contaId AS long) IS NULL OR e.contaBancaria.id = :contaId)
              AND (CAST(:cartaoId AS long) IS NULL OR e.cartaoCredito.id = :cartaoId)
              AND (CAST(:paga AS boolean) IS NULL OR e.paga = :paga)
            """)
    Page<Expense> findAllByFilters(@Param("userIds") List<Long> userIds,
                                    @Param("dataInicio") LocalDate dataInicio,
                                    @Param("dataFim") LocalDate dataFim,
                                    @Param("categoriaId") Long categoriaId,
                                    @Param("contaId") Long contaId,
                                    @Param("cartaoId") Long cartaoId,
                                    @Param("paga") Boolean paga,
                                    Pageable pageable);

    @Query("""
            SELECT e FROM Expense e
            LEFT JOIN FETCH e.categoria
            LEFT JOIN FETCH e.contaBancaria
            LEFT JOIN FETCH e.cartaoCredito
            WHERE e.user.id IN :userIds
              AND (CAST(:dataInicio AS date) IS NULL OR e.data >= :dataInicio)
              AND (CAST(:dataFim AS date) IS NULL OR e.data <= :dataFim)
              AND (CAST(:categoriaId AS long) IS NULL OR e.categoria.id = :categoriaId)
              AND (CAST(:contaId AS long) IS NULL OR e.contaBancaria.id = :contaId)
              AND (CAST(:cartaoId AS long) IS NULL OR e.cartaoCredito.id = :cartaoId)
              AND (CAST(:paga AS boolean) IS NULL OR e.paga = :paga)
            """)
    List<Expense> findAllByFiltersList(@Param("userIds") List<Long> userIds,
                                        @Param("dataInicio") LocalDate dataInicio,
                                        @Param("dataFim") LocalDate dataFim,
                                        @Param("categoriaId") Long categoriaId,
                                        @Param("contaId") Long contaId,
                                        @Param("cartaoId") Long cartaoId,
                                        @Param("paga") Boolean paga);

    @Query("""
            SELECT e FROM Expense e
            WHERE e.user.id IN :userIds AND e.data BETWEEN :dataInicio AND :dataFim
            """)
    List<Expense> findAllByUserIdAndDataBetween(@Param("userIds") List<Long> userIds,
                                                 @Param("dataInicio") LocalDate dataInicio,
                                                 @Param("dataFim") LocalDate dataFim);

    @Query("SELECT COALESCE(SUM(e.valor), 0) FROM Expense e WHERE e.user.id IN :userIds AND e.data BETWEEN :dataInicio AND :dataFim")
    BigDecimal sumValorByUserIdAndDataBetween(@Param("userIds") List<Long> userIds,
                                               @Param("dataInicio") LocalDate dataInicio,
                                               @Param("dataFim") LocalDate dataFim);

    @Query("""
            SELECT COALESCE(SUM(e.valor), 0) FROM Expense e
            WHERE e.user.id IN :userIds AND e.data BETWEEN :dataInicio AND :dataFim AND e.categoria.isInvestment = true
            """)
    BigDecimal sumValorByUserIdAndDataBetweenAndCategoriaIsInvestment(@Param("userIds") List<Long> userIds,
                                                                       @Param("dataInicio") LocalDate dataInicio,
                                                                       @Param("dataFim") LocalDate dataFim);

    @Query("""
            SELECT e FROM Expense e
            WHERE e.user.id IN :userIds AND e.paga = false AND e.data BETWEEN :dataInicio AND :dataFim
            ORDER BY e.data ASC
            """)
    List<Expense> findUpcomingUnpaid(@Param("userIds") List<Long> userIds,
                                      @Param("dataInicio") LocalDate dataInicio,
                                      @Param("dataFim") LocalDate dataFim);

}
