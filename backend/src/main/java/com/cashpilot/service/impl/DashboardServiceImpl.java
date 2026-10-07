package com.cashpilot.service.impl;

import com.cashpilot.dto.response.DashboardSummaryResponseDTO;
import com.cashpilot.dto.response.MetaPrincipalResponseDTO;
import com.cashpilot.dto.response.ProximaContaResponseDTO;
import com.cashpilot.dto.response.SaldoHistoricoPontoDTO;
import com.cashpilot.entity.Expense;
import com.cashpilot.entity.FinancialGoal;
import com.cashpilot.repository.ExpenseRepository;
import com.cashpilot.repository.FinancialGoalRepository;
import com.cashpilot.repository.IncomeRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.BankAccountService;
import com.cashpilot.service.DashboardService;
import com.cashpilot.service.SaldoHistoricoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final BankAccountService bankAccountService;
    private final SaldoHistoricoService saldoHistoricoService;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final FinancialGoalRepository financialGoalRepository;
    private final CurrentUserProvider currentUserProvider;

    @Value("${cashpilot.dashboard.proximas-contas-dias:7}")
    private int proximasContasDiasPadrao;

    @Value("${cashpilot.dashboard.evolucao-saldo-dias-padrao:30}")
    private int evolucaoSaldoDiasPadrao;

    @Override
    public DashboardSummaryResponseDTO getResumo() {
        List<Long> scopeUserIds = currentUserProvider.getScopeUserIds();

        BigDecimal saldoAtual = bankAccountService.getSaldoAtualTotal();

        LocalDate hoje = LocalDate.now();
        LocalDate primeiroDiaMes = hoje.withDayOfMonth(1);
        LocalDate ultimoDiaMes = hoje.withDayOfMonth(hoje.lengthOfMonth());

        BigDecimal entradasMes = incomeRepository.sumValorByUserIdAndDataBetween(scopeUserIds, primeiroDiaMes, ultimoDiaMes);
        BigDecimal saidasMes = expenseRepository.sumValorByUserIdAndDataBetween(scopeUserIds, primeiroDiaMes, ultimoDiaMes);
        BigDecimal investimentosMes = expenseRepository.sumValorByUserIdAndDataBetweenAndCategoriaIsInvestment(scopeUserIds, primeiroDiaMes, ultimoDiaMes);

        MetaPrincipalResponseDTO metaPrincipal = financialGoalRepository.findActiveOrderedByNearestDataAlvo(scopeUserIds, hoje)
                .stream()
                .findFirst()
                .map(this::toMetaPrincipal)
                .orElse(null);

        LocalDate fimJanela = hoje.plusDays(proximasContasDiasPadrao);
        List<ProximaContaResponseDTO> proximasContas = expenseRepository.findUpcomingUnpaid(scopeUserIds, hoje, fimJanela)
                .stream()
                .limit(10)
                .map(e -> toProximaConta(e, hoje))
                .toList();

        return new DashboardSummaryResponseDTO(saldoAtual, entradasMes, saidasMes, investimentosMes, metaPrincipal, proximasContas);
    }

    @Override
    public List<SaldoHistoricoPontoDTO> getEvolucaoSaldo(Integer dias) {
        return saldoHistoricoService.getHistoricoTotal(dias != null ? dias : evolucaoSaldoDiasPadrao);
    }

    private MetaPrincipalResponseDTO toMetaPrincipal(FinancialGoal goal) {
        BigDecimal progresso = calcularProgresso(goal.getValorAtual(), goal.getValorAlvo());
        return new MetaPrincipalResponseDTO(goal.getId(), goal.getNome(), goal.getValorAlvo(), goal.getValorAtual(), progresso, goal.getDataAlvo());
    }

    private BigDecimal calcularProgresso(BigDecimal valorAtual, BigDecimal valorAlvo) {
        if (valorAlvo == null || valorAlvo.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal progresso = valorAtual.divide(valorAlvo, 4, RoundingMode.HALF_UP).multiply(HUNDRED);
        return progresso.compareTo(HUNDRED) > 0 ? HUNDRED : progresso;
    }

    private ProximaContaResponseDTO toProximaConta(Expense expense, LocalDate hoje) {
        long diasRestantes = ChronoUnit.DAYS.between(hoje, expense.getData());
        String categoriaNome = expense.getCategoria() != null ? expense.getCategoria().getNome() : null;
        return new ProximaContaResponseDTO(expense.getId(), expense.getDescricao(), expense.getValor(), expense.getData(), categoriaNome, diasRestantes);
    }

}
