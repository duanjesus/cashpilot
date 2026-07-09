package com.cashpilot.service;

import com.cashpilot.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProjectionCalculator")
class ProjectionCalculatorTest {

    private final ProjectionCalculator calculator = new ProjectionCalculator();

    @Test
    @DisplayName("Taxa zero: meses = ceil((alvo - patrimonio) / aporte)")
    void deveCalcularCasoLinearComTaxaZero() {
        ProjectionResult result = calculator.calculate(
                null, null, null,
                BigDecimal.valueOf(1000), BigDecimal.ZERO, BigDecimal.valueOf(12000), BigDecimal.ZERO);

        assertThat(result.atingivel()).isTrue();
        assertThat(result.mesesParaAtingir()).isEqualTo(12);
        assertThat(result.anos()).isEqualTo(1);
        assertThat(result.mesesRestantes()).isEqualTo(0);
        assertThat(result.valorFinalProjetado()).isEqualByComparingTo("12000.00");
    }

    @Test
    @DisplayName("Taxa positiva: resolve via fórmula de valor futuro de anuidade")
    void deveCalcularCasoComTaxaPositiva() {
        ProjectionResult result = calculator.calculate(
                null, null, null,
                BigDecimal.valueOf(100), BigDecimal.valueOf(1000), BigDecimal.valueOf(2000), BigDecimal.valueOf(0.01));

        assertThat(result.atingivel()).isTrue();
        assertThat(result.mesesParaAtingir()).isEqualTo(9);
        assertThat(result.valorFinalProjetado()).isGreaterThanOrEqualTo(BigDecimal.valueOf(2000));
    }

    @Test
    @DisplayName("Patrimônio já atingiu o alvo: meses = 0")
    void deveRetornarZeroMesesQuandoJaAtingiuOAlvo() {
        ProjectionResult result = calculator.calculate(
                null, null, null,
                BigDecimal.valueOf(500), BigDecimal.valueOf(5000), BigDecimal.valueOf(4000), BigDecimal.valueOf(0.01));

        assertThat(result.atingivel()).isTrue();
        assertThat(result.mesesParaAtingir()).isEqualTo(0);
        assertThat(result.valorFinalProjetado()).isEqualByComparingTo("4000.00");
    }

    @Test
    @DisplayName("Inatingível quando aporte <= 0 e patrimônio não cresce o suficiente")
    void deveRetornarInatingivelQuandoAporteNegativoEPatrimonioInsuficiente() {
        ProjectionResult result = calculator.calculate(
                null, null, null,
                BigDecimal.valueOf(-50), BigDecimal.valueOf(100), BigDecimal.valueOf(1_000_000), BigDecimal.valueOf(0.001));

        assertThat(result.atingivel()).isFalse();
        assertThat(result.mesesParaAtingir()).isNull();
        assertThat(result.dataEstimada()).isNull();
        assertThat(result.valorFinalProjetado()).isNull();
    }

    @Test
    @DisplayName("Inatingível quando patrimônio é zero e não há aporte positivo (taxa zero)")
    void deveRetornarInatingivelSemAporteETaxaZero() {
        ProjectionResult result = calculator.calculate(
                null, null, null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1000), BigDecimal.ZERO);

        assertThat(result.atingivel()).isFalse();
        assertThat(result.mesesParaAtingir()).isNull();
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando o valor alvo não é positivo")
    void deveLancarExcecaoQuandoValorAlvoInvalido() {
        assertThatThrownBy(() -> calculator.calculate(
                null, null, null,
                BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(0.01)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando a taxa de retorno é negativa")
    void deveLancarExcecaoQuandoTaxaNegativa() {
        assertThatThrownBy(() -> calculator.calculate(
                null, null, null,
                BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.valueOf(1000), BigDecimal.valueOf(-0.01)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Deve derivar o aporte mensal de salário menos despesas quando investimentoMensal é nulo")
    void deveDerivarAporteMensalQuandoNaoInformado() {
        ProjectionResult result = calculator.calculate(
                BigDecimal.valueOf(5000), BigDecimal.valueOf(2000), BigDecimal.valueOf(1000),
                null, BigDecimal.ZERO, BigDecimal.valueOf(24000), BigDecimal.ZERO);

        assertThat(result.aporteMensal()).isEqualByComparingTo("2000.00");
        assertThat(result.mesesParaAtingir()).isEqualTo(12);
    }

    @Test
    @DisplayName("simularEvolucaoMensal: sem aporte e sem taxa, o valor permanece constante")
    void deveSimularEvolucaoConstanteSemAporteESemTaxa() {
        List<BigDecimal> valores = calculator.simularEvolucaoMensal(
                BigDecimal.valueOf(1000), BigDecimal.ZERO, BigDecimal.ZERO, 3);

        assertThat(valores).hasSize(4);
        assertThat(valores).allSatisfy(v -> assertThat(v).isEqualByComparingTo("1000.00"));
    }

    @Test
    @DisplayName("simularEvolucaoMensal: com aporte e taxa positivos, aplica valor[n] = valor[n-1] * (1+i) + aporte")
    void deveSimularEvolucaoComAporteETaxaPositivos() {
        List<BigDecimal> valores = calculator.simularEvolucaoMensal(
                BigDecimal.valueOf(1000), BigDecimal.valueOf(100), BigDecimal.valueOf(0.01), 3);

        assertThat(valores).hasSize(4);
        assertThat(valores.get(0)).isEqualByComparingTo("1000.00");
        assertThat(valores.get(1)).isEqualByComparingTo("1110.00");   // 1000*1.01 + 100
        assertThat(valores.get(2)).isEqualByComparingTo("1221.10");   // 1110*1.01 + 100
        assertThat(valores.get(3)).isEqualByComparingTo("1333.31");   // 1221.10*1.01 + 100 = 1333.311
    }

    @Test
    @DisplayName("simularEvolucaoMensal: horizonte zero retorna lista de um elemento igual ao patrimônio inicial")
    void deveRetornarListaDeUmElementoQuandoHorizonteZero() {
        List<BigDecimal> valores = calculator.simularEvolucaoMensal(
                BigDecimal.valueOf(2500), BigDecimal.valueOf(200), BigDecimal.valueOf(0.02), 0);

        assertThat(valores).hasSize(1);
        assertThat(valores.get(0)).isEqualByComparingTo("2500.00");
    }

}
