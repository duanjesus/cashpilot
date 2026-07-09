package com.cashpilot.service;

import com.cashpilot.dto.response.PrevisaoSaldoResponseDTO;
import com.cashpilot.dto.response.RelatorioMensalDTO;
import com.cashpilot.service.impl.PrevisaoSaldoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PrevisaoSaldoService")
class PrevisaoSaldoServiceImplTest {

    @Mock
    private RelatorioService relatorioService;

    @Mock
    private BankAccountService bankAccountService;

    private PrevisaoSaldoServiceImpl previsaoSaldoService;

    @BeforeEach
    void setUp() {
        previsaoSaldoService = new PrevisaoSaldoServiceImpl(relatorioService, bankAccountService);
    }

    @Test
    @DisplayName("Calcula a média mensal histórica e projeta o saldo linearmente a partir do saldo atual")
    void deveCalcularMediaEProjetarSaldo() {
        when(relatorioService.getRelatorioMensal(3)).thenReturn(List.of(
                new RelatorioMensalDTO("2026-05", BigDecimal.valueOf(5000), BigDecimal.valueOf(4000), BigDecimal.ZERO, BigDecimal.valueOf(1000)),
                new RelatorioMensalDTO("2026-06", BigDecimal.valueOf(5000), BigDecimal.valueOf(4500), BigDecimal.ZERO, BigDecimal.valueOf(500)),
                new RelatorioMensalDTO("2026-07", BigDecimal.valueOf(5000), BigDecimal.valueOf(4700), BigDecimal.ZERO, BigDecimal.valueOf(300))
        ));
        when(bankAccountService.getSaldoAtualTotal()).thenReturn(BigDecimal.valueOf(10000));

        PrevisaoSaldoResponseDTO result = previsaoSaldoService.getPrevisao(3, 2);

        assertThat(result.saldoAtual()).isEqualByComparingTo("10000");
        assertThat(result.mediaMensalHistorica()).isEqualByComparingTo("600.00"); // (1000+500+300)/3
        assertThat(result.serie()).hasSize(2);
        assertThat(result.serie().get(0).saldoProjetado()).isEqualByComparingTo("10600.00");
        assertThat(result.serie().get(1).saldoProjetado()).isEqualByComparingTo("11200.00");
    }

    @Test
    @DisplayName("Usa os padrões (6 meses de histórico, 12 de projeção) quando os parâmetros são nulos")
    void deveUsarPadroesQuandoParametrosNulos() {
        when(relatorioService.getRelatorioMensal(6)).thenReturn(List.of(
                new RelatorioMensalDTO("2026-01", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(100))
        ));
        when(bankAccountService.getSaldoAtualTotal()).thenReturn(BigDecimal.ZERO);

        PrevisaoSaldoResponseDTO result = previsaoSaldoService.getPrevisao(null, null);

        assertThat(result.serie()).hasSize(12);
        assertThat(result.mediaMensalHistorica()).isEqualByComparingTo("100.00");
    }

}
