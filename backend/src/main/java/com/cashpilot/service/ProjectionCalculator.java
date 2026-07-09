package com.cashpilot.service;

import com.cashpilot.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Pure math for "when will I reach my net-worth target" projections. No persistence
 * dependencies on purpose — trivially unit-testable without mocks.
 */
@Component
public class ProjectionCalculator {

    private static final int HORIZONTE_MAXIMO_MESES = 1200; // 100 years

    public ProjectionResult calculate(BigDecimal salario,
                                       BigDecimal despesasFixas,
                                       BigDecimal despesasVariaveis,
                                       BigDecimal investimentoMensal,
                                       BigDecimal patrimonioAtual,
                                       BigDecimal valorAlvo,
                                       BigDecimal taxaRetornoMensal) {

        double patrimonio = toDouble(patrimonioAtual);
        double alvo = toDouble(valorAlvo);
        double i = toDouble(taxaRetornoMensal);

        if (i < 0) {
            throw new BusinessException("A taxa de retorno mensal não pode ser negativa");
        }
        if (alvo <= 0) {
            throw new BusinessException("O valor alvo deve ser maior que zero");
        }

        double a = investimentoMensal != null
                ? toDouble(investimentoMensal)
                : toDouble(salario) - toDouble(despesasFixas) - toDouble(despesasVariaveis);

        if (alvo <= patrimonio) {
            return build(0, a, patrimonio, i, alvo);
        }

        if (i == 0) {
            if (a <= 0) {
                return unreachable(a);
            }
            int n = (int) Math.ceil((alvo - patrimonio) / a);
            return build(n, a, patrimonio, i, alvo);
        }

        // i > 0
        if (a <= 0) {
            if (patrimonio <= 0) {
                return unreachable(a);
            }
            int nSimulado = simulateMonthsUntilReached(patrimonio, a, i, alvo, HORIZONTE_MAXIMO_MESES);
            if (nSimulado < 0) {
                return unreachable(a);
            }
            return build(nSimulado, a, patrimonio, i, alvo);
        }

        double aOverI = a / i;
        double x = (alvo + aOverI) / (patrimonio + aOverI);
        if (x <= 0) {
            return unreachable(a);
        }
        int n = (int) Math.ceil(Math.log(x) / Math.log(1 + i));
        if (n < 0) {
            n = 0;
        }
        return build(n, a, patrimonio, i, alvo);
    }

    private int simulateMonthsUntilReached(double patrimonioInicial, double aporte, double i, double alvo, int maxMeses) {
        double saldo = patrimonioInicial;
        for (int mes = 1; mes <= maxMeses; mes++) {
            saldo = saldo * (1 + i) + aporte;
            if (saldo >= alvo) {
                return mes;
            }
        }
        return -1;
    }

    private ProjectionResult unreachable(double aporteMensal) {
        return new ProjectionResult(false, null, null, null, null, null, toBigDecimal(aporteMensal));
    }

    private ProjectionResult build(int n, double aporte, double patrimonio, double i, double alvo) {
        BigDecimal valorFinalProjetado;
        if (n == 0) {
            valorFinalProjetado = toBigDecimal(alvo);
        } else if (i == 0) {
            valorFinalProjetado = toBigDecimal(patrimonio + aporte * n);
        } else {
            double fatorCrescimento = Math.pow(1 + i, n);
            double valorFinal = patrimonio * fatorCrescimento + aporte * (fatorCrescimento - 1) / i;
            valorFinalProjetado = toBigDecimal(valorFinal);
        }

        Integer anos = n / 12;
        Integer mesesRestantes = n % 12;
        LocalDate dataEstimada = LocalDate.now().plusMonths(n);

        return new ProjectionResult(true, n, anos, mesesRestantes, dataEstimada, valorFinalProjetado, toBigDecimal(aporte));
    }

    private double toDouble(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private BigDecimal toBigDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

}
