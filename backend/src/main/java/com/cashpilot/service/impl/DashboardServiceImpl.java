package com.cashpilot.service.impl;

import com.cashpilot.dto.response.DashboardSummaryResponseDTO;
import com.cashpilot.dto.response.EvolucaoSaldoPointDTO;
import com.cashpilot.dto.response.MetaPrincipalResponseDTO;
import com.cashpilot.dto.response.ProximaContaResponseDTO;
import com.cashpilot.entity.BankAccount;
import com.cashpilot.entity.Expense;
import com.cashpilot.entity.FinancialGoal;
import com.cashpilot.entity.Income;
import com.cashpilot.repository.BankAccountRepository;
import com.cashpilot.repository.ExpenseRepository;
import com.cashpilot.repository.FinancialGoalRepository;
import com.cashpilot.repository.IncomeRepository;
import com.cashpilot.repository.TransferRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final BankAccountRepository bankAccountRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final TransferRepository transferRepository;
    private final FinancialGoalRepository financialGoalRepository;
    private final CurrentUserProvider currentUserProvider;

    @Value("${cashpilot.dashboard.proximas-contas-dias:7}")
    private int proximasContasDiasPadrao;

    @Value("${cashpilot.dashboard.evolucao-saldo-dias-padrao:30}")
    private int evolucaoSaldoDiasPadrao;

    @Override
    public DashboardSummaryResponseDTO getResumo() {
        Long userId = currentUserProvider.getCurrentUserId();

        BigDecimal saldoAtual = calcularSaldoAtualTotal(userId);

        LocalDate hoje = LocalDate.now();
        LocalDate primeiroDiaMes = hoje.withDayOfMonth(1);
        LocalDate ultimoDiaMes = hoje.withDayOfMonth(hoje.lengthOfMonth());

        BigDecimal entradasMes = incomeRepository.sumValorByUserIdAndDataBetween(userId, primeiroDiaMes, ultimoDiaMes);
        BigDecimal saidasMes = expenseRepository.sumValorByUserIdAndDataBetween(userId, primeiroDiaMes, ultimoDiaMes);
        BigDecimal investimentosMes = expenseRepository.sumValorByUserIdAndDataBetweenAndCategoriaIsInvestment(userId, primeiroDiaMes, ultimoDiaMes);

        MetaPrincipalResponseDTO metaPrincipal = financialGoalRepository.findActiveOrderedByNearestDataAlvo(userId, hoje)
                .stream()
                .findFirst()
                .map(this::toMetaPrincipal)
                .orElse(null);

        LocalDate fimJanela = hoje.plusDays(proximasContasDiasPadrao);
        List<ProximaContaResponseDTO> proximasContas = expenseRepository.findUpcomingUnpaid(userId, hoje, fimJanela)
                .stream()
                .limit(10)
                .map(e -> toProximaConta(e, hoje))
                .toList();

        return new DashboardSummaryResponseDTO(saldoAtual, entradasMes, saidasMes, investimentosMes, metaPrincipal, proximasContas);
    }

    @Override
    public List<EvolucaoSaldoPointDTO> getEvolucaoSaldo(Integer dias) {
        Long userId = currentUserProvider.getCurrentUserId();
        int janela = dias != null ? dias : evolucaoSaldoDiasPadrao;

        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.minusDays(janela - 1L);

        Map<LocalDate, BigDecimal> deltasPorDia = new TreeMap<>();
        for (LocalDate d = inicio; !d.isAfter(hoje); d = d.plusDays(1)) {
            deltasPorDia.put(d, BigDecimal.ZERO);
        }

        for (Income income : incomeRepository.findAllByUserIdAndDataBetween(userId, inicio, hoje)) {
            deltasPorDia.merge(income.getData(), income.getValor(), BigDecimal::add);
        }
        for (Expense expense : expenseRepository.findAllByUserIdAndDataBetween(userId, inicio, hoje)) {
            if (expense.getContaBancaria() != null) {
                deltasPorDia.merge(expense.getData(), expense.getValor().negate(), BigDecimal::add);
            }
        }

        List<EvolucaoSaldoPointDTO> pontos = new java.util.ArrayList<>();
        BigDecimal acumulado = BigDecimal.ZERO;
        for (Map.Entry<LocalDate, BigDecimal> entry : deltasPorDia.entrySet()) {
            acumulado = acumulado.add(entry.getValue());
            pontos.add(new EvolucaoSaldoPointDTO(entry.getKey(), acumulado));
        }
        return pontos;
    }

    private BigDecimal calcularSaldoAtualTotal(Long userId) {
        return bankAccountRepository.findAllByUserId(userId).stream()
                .filter(BankAccount::getAtiva)
                .map(this::calcularSaldoAtual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularSaldoAtual(BankAccount account) {
        Long contaId = account.getId();
        BigDecimal entradas = incomeRepository.sumValorByContaBancariaId(contaId);
        BigDecimal saidas = expenseRepository.sumValorByContaBancariaId(contaId);
        BigDecimal transferenciasSaida = transferRepository.sumValorByContaOrigemId(contaId);
        BigDecimal transferenciasEntrada = transferRepository.sumValorByContaDestinoId(contaId);

        return account.getSaldoInicial()
                .add(entradas)
                .subtract(saidas)
                .subtract(transferenciasSaida)
                .add(transferenciasEntrada);
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
