package com.cashpilot.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SubscriptionChargeScheduler")
class SubscriptionChargeSchedulerTest {

    private final SubscriptionChargeScheduler scheduler = new SubscriptionChargeScheduler();

    @Test
    @DisplayName("Deve gerar uma data por mês dentro da janela informada")
    void deveGerarDatasMensais() {
        List<LocalDate> datas = scheduler.calcularDatasDevidas(
                LocalDate.of(2026, 1, 10), null, 10, LocalDate.of(2026, 4, 10));

        assertThat(datas).containsExactly(
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 2, 10),
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 4, 10)
        );
    }

    @Test
    @DisplayName("Deve limitar o dia de cobrança ao último dia do mês (ex.: dia 31 em fevereiro)")
    void deveLimitarDiaDeCobrancaAoUltimoDiaDoMes() {
        List<LocalDate> datas = scheduler.calcularDatasDevidas(
                LocalDate.of(2026, 1, 31), null, 31, LocalDate.of(2026, 2, 28));

        assertThat(datas).containsExactly(
                LocalDate.of(2026, 1, 31),
                LocalDate.of(2026, 2, 28)
        );
    }

    @Test
    @DisplayName("Deve excluir a cobrança do primeiro mês quando a data de cobrança cai antes da data de início")
    void deveExcluirCobrancaAntesDaDataDeInicio() {
        List<LocalDate> datas = scheduler.calcularDatasDevidas(
                LocalDate.of(2026, 1, 15), null, 10, LocalDate.of(2026, 3, 10));

        assertThat(datas).containsExactly(
                LocalDate.of(2026, 2, 10),
                LocalDate.of(2026, 3, 10)
        );
    }

    @Test
    @DisplayName("Deve excluir datas após o mês de dataFim")
    void deveExcluirDatasAposDataFim() {
        List<LocalDate> datas = scheduler.calcularDatasDevidas(
                LocalDate.of(2026, 1, 10), LocalDate.of(2026, 2, 15), 10, LocalDate.of(2026, 6, 10));

        assertThat(datas).containsExactly(
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 2, 10)
        );
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando a data de início é posterior à data limite")
    void deveRetornarVazioQuandoInicioAposAteData() {
        List<LocalDate> datas = scheduler.calcularDatasDevidas(
                LocalDate.of(2026, 6, 10), null, 10, LocalDate.of(2026, 1, 1));

        assertThat(datas).isEmpty();
    }

    @Test
    @DisplayName("Deve gerar o backlog completo quando a data de início é vários meses anterior à data limite")
    void deveGerarBacklogCompleto() {
        List<LocalDate> datas = scheduler.calcularDatasDevidas(
                LocalDate.of(2025, 1, 10), null, 10, LocalDate.of(2026, 1, 10));

        assertThat(datas).hasSize(13);
    }

}
