package com.cashpilot.repository;

import com.cashpilot.entity.Income;
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

    Optional<Income> findByIdAndUserId(Long id, Long userId);

    boolean existsByCategoriaId(Long categoriaId);

    boolean existsByContaBancariaId(Long contaId);

    @Query("SELECT COALESCE(SUM(i.valor), 0) FROM Income i WHERE i.contaBancaria.id = :contaId")
    BigDecimal sumValorByContaBancariaId(@Param("contaId") Long contaId);

    @Query("""
            SELECT i FROM Income i
            WHERE i.user.id = :userId
              AND (CAST(:dataInicio AS date) IS NULL OR i.data >= :dataInicio)
              AND (CAST(:dataFim AS date) IS NULL OR i.data <= :dataFim)
              AND (CAST(:categoriaId AS long) IS NULL OR i.categoria.id = :categoriaId)
              AND (CAST(:contaId AS long) IS NULL OR i.contaBancaria.id = :contaId)
            """)
    Page<Income> findAllByFilters(@Param("userId") Long userId,
                                   @Param("dataInicio") LocalDate dataInicio,
                                   @Param("dataFim") LocalDate dataFim,
                                   @Param("categoriaId") Long categoriaId,
                                   @Param("contaId") Long contaId,
                                   Pageable pageable);

    @Query("""
            SELECT i FROM Income i
            WHERE i.user.id = :userId AND i.data BETWEEN :dataInicio AND :dataFim
            """)
    List<Income> findAllByUserIdAndDataBetween(@Param("userId") Long userId,
                                                @Param("dataInicio") LocalDate dataInicio,
                                                @Param("dataFim") LocalDate dataFim);

    @Query("SELECT COALESCE(SUM(i.valor), 0) FROM Income i WHERE i.user.id = :userId AND i.data BETWEEN :dataInicio AND :dataFim")
    BigDecimal sumValorByUserIdAndDataBetween(@Param("userId") Long userId,
                                               @Param("dataInicio") LocalDate dataInicio,
                                               @Param("dataFim") LocalDate dataFim);

}
