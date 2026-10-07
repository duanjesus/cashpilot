package com.cashpilot.repository;

import com.cashpilot.entity.SaldoDiario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaldoDiarioRepository extends JpaRepository<SaldoDiario, Long> {

    List<SaldoDiario> findAllByContaBancariaIdAndDataBetween(Long contaId, LocalDate inicio, LocalDate fim);

    @Query("SELECT MAX(s.data) FROM SaldoDiario s WHERE s.contaBancaria.id = :contaId")
    Optional<LocalDate> findUltimaData(@Param("contaId") Long contaId);

}
