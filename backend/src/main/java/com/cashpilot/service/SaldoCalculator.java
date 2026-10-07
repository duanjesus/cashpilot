package com.cashpilot.service;

import com.cashpilot.entity.BankAccount;
import com.cashpilot.repository.ExpenseRepository;
import com.cashpilot.repository.IncomeRepository;
import com.cashpilot.repository.TransferRepository;
import com.cashpilot.repository.projection.MovimentoDiario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Single definition of a bank account's realized balance: {@code saldoInicial} plus money that
 * has actually moved — received income, paid expenses and transfers — up to a given day.
 * Unpaid/unreceived entries and anything dated after that day are excluded.
 */
@Component
@RequiredArgsConstructor
public class SaldoCalculator {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final TransferRepository transferRepository;

    public BigDecimal saldoEm(BankAccount conta, LocalDate dia) {
        Long contaId = conta.getId();
        return conta.getSaldoInicial()
                .add(incomeRepository.sumRealizadoByContaBancariaIdAte(contaId, dia))
                .subtract(expenseRepository.sumRealizadoByContaBancariaIdAte(contaId, dia))
                .subtract(transferRepository.sumValorByContaOrigemIdAte(contaId, dia))
                .add(transferRepository.sumValorByContaDestinoIdAte(contaId, dia));
    }

    /** End-of-day balance for every day in {@code [inicio, fim]}. */
    public SortedMap<LocalDate, BigDecimal> serieDiaria(BankAccount conta, LocalDate inicio, LocalDate fim) {
        Long contaId = conta.getId();
        Map<LocalDate, BigDecimal> deltas = new HashMap<>();
        somar(deltas, incomeRepository.sumRealizadoPorDia(contaId, inicio, fim), false);
        somar(deltas, expenseRepository.sumRealizadoPorDia(contaId, inicio, fim), true);
        somar(deltas, transferRepository.sumSaidasPorDia(contaId, inicio, fim), true);
        somar(deltas, transferRepository.sumEntradasPorDia(contaId, inicio, fim), false);

        return acumular(saldoEm(conta, inicio.minusDays(1)), deltas, inicio, fim);
    }

    static SortedMap<LocalDate, BigDecimal> acumular(BigDecimal saldoBase, Map<LocalDate, BigDecimal> deltas,
                                                     LocalDate inicio, LocalDate fim) {
        SortedMap<LocalDate, BigDecimal> serie = new TreeMap<>();
        BigDecimal saldo = saldoBase;
        for (LocalDate dia = inicio; !dia.isAfter(fim); dia = dia.plusDays(1)) {
            saldo = saldo.add(deltas.getOrDefault(dia, BigDecimal.ZERO));
            serie.put(dia, saldo);
        }
        return serie;
    }

    private void somar(Map<LocalDate, BigDecimal> deltas, List<MovimentoDiario> movimentos, boolean saida) {
        for (MovimentoDiario movimento : movimentos) {
            BigDecimal valor = saida ? movimento.total().negate() : movimento.total();
            deltas.merge(movimento.dia(), valor, BigDecimal::add);
        }
    }

}
