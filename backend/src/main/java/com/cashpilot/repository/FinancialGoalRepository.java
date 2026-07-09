package com.cashpilot.repository;

import com.cashpilot.entity.FinancialGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, Long> {

    List<FinancialGoal> findAllByUserId(Long userId);

    Optional<FinancialGoal> findByIdAndUserId(Long id, Long userId);

    @Query("""
            SELECT g FROM FinancialGoal g
            WHERE g.user.id = :userId AND g.ativa = true AND g.dataAlvo >= :hoje
            ORDER BY g.dataAlvo ASC
            """)
    List<FinancialGoal> findActiveOrderedByNearestDataAlvo(@Param("userId") Long userId, @Param("hoje") LocalDate hoje);

}
