package com.cashpilot.service;

import com.cashpilot.entity.BankAccount;
import com.cashpilot.repository.ExpenseRepository;
import com.cashpilot.repository.IncomeRepository;
import com.cashpilot.repository.TransferRepository;
import com.cashpilot.repository.projection.MovimentoDiario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaldoCalculator")
class SaldoCalculatorTest {

    private static final LocalDate DIA_10 = LocalDate.of(2026, 3, 10);

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private TransferRepository transferRepository;

    @InjectMocks
    private SaldoCalculator saldoCalculator;

    private BankAccount conta;

    @BeforeEach
    void setUp() {
        conta = BankAccount.builder().id(10L).saldoInicial(new BigDecimal("1000.00"))
                .dataSaldoInicial(LocalDate.of(2026, 1, 1)).build();
    }

    private void stubSaldoAte(LocalDate dia, String receitas, String despesas, String saidas, String entradas) {
        when(incomeRepository.sumRealizadoByContaBancariaIdAte(10L, dia)).thenReturn(new BigDecimal(receitas));
        when(expenseRepository.sumRealizadoByContaBancariaIdAte(10L, dia)).thenReturn(new BigDecimal(despesas));
        when(transferRepository.sumValorByContaOrigemIdAte(10L, dia)).thenReturn(new BigDecimal(saidas));
        when(transferRepository.sumValorByContaDestinoIdAte(10L, dia)).thenReturn(new BigDecimal(entradas));
    }

    @Test
    @DisplayName("Saldo do dia soma receitas e transferências recebidas e subtrai despesas e transferências enviadas")
    void deveComporSaldoDoDia() {
        stubSaldoAte(DIA_10, "500.00", "200.00", "50.00", "30.00");

        assertThat(saldoCalculator.saldoEm(conta, DIA_10)).isEqualByComparingTo("1280.00");
    }

    @Test
    @DisplayName("Série diária parte do saldo da véspera e aplica os movimentos de cada dia")
    void deveMontarSerieDiariaAPartirDoSaldoDaVespera() {
        LocalDate inicio = DIA_10;
        LocalDate fim = DIA_10.plusDays(2);
        stubSaldoAte(inicio.minusDays(1), "0", "0", "0", "0");
        when(incomeRepository.sumRealizadoPorDia(10L, inicio, fim))
                .thenReturn(List.of(new MovimentoDiario(inicio, new BigDecimal("300.00"))));
        when(expenseRepository.sumRealizadoPorDia(10L, inicio, fim))
                .thenReturn(List.of(new MovimentoDiario(fim, new BigDecimal("120.00"))));
        when(transferRepository.sumSaidasPorDia(10L, inicio, fim))
                .thenReturn(List.of(new MovimentoDiario(inicio, new BigDecimal("100.00"))));
        when(transferRepository.sumEntradasPorDia(10L, inicio, fim))
                .thenReturn(List.of(new MovimentoDiario(fim, new BigDecimal("20.00"))));

        SortedMap<LocalDate, BigDecimal> serie = saldoCalculator.serieDiaria(conta, inicio, fim);

        assertThat(serie).hasSize(3);
        assertThat(serie.get(inicio)).isEqualByComparingTo("1200.00");
        assertThat(serie.get(inicio.plusDays(1))).isEqualByComparingTo("1200.00");
        assertThat(serie.get(fim)).isEqualByComparingTo("1100.00");
    }

    @Test
    @DisplayName("Acumular repete o saldo anterior nos dias sem movimento")
    void deveRepetirSaldoEmDiasSemMovimento() {
        SortedMap<LocalDate, BigDecimal> serie = SaldoCalculator.acumular(
                new BigDecimal("50"), Map.of(DIA_10.plusDays(1), new BigDecimal("-20")), DIA_10, DIA_10.plusDays(2));

        assertThat(serie.values()).usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("50"), new BigDecimal("30"), new BigDecimal("30"));
    }

}
