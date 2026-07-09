package com.cashpilot.service;

import com.cashpilot.dto.response.FluxoCaixaPontoDTO;
import com.cashpilot.dto.response.FluxoCaixaResponseDTO;
import com.cashpilot.entity.BankAccount;
import com.cashpilot.entity.Category;
import com.cashpilot.entity.Expense;
import com.cashpilot.entity.Income;
import com.cashpilot.entity.Subscription;
import com.cashpilot.entity.User;
import com.cashpilot.repository.ExpenseRepository;
import com.cashpilot.repository.IncomeRepository;
import com.cashpilot.repository.SubscriptionRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.impl.FluxoCaixaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FluxoCaixaService")
class FluxoCaixaServiceImplTest {

    @Mock
    private BankAccountService bankAccountService;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private FluxoCaixaServiceImpl fluxoCaixaService;

    private User user;
    private LocalDate hoje;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Ana").email("ana@cashpilot.com").password("hash").build();
        hoje = LocalDate.now();
        fluxoCaixaService = new FluxoCaixaServiceImpl(
                bankAccountService, expenseRepository, incomeRepository, subscriptionRepository,
                new SubscriptionChargeScheduler(), currentUserProvider);
    }

    @Test
    @DisplayName("Saldo inicial é igual ao saldo atual total das contas bancárias")
    void deveRetornarSaldoInicialIgualAoSaldoAtualTotal() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(bankAccountService.getSaldoAtualTotal()).thenReturn(BigDecimal.valueOf(1000));
        when(expenseRepository.findUpcomingUnpaid(anyLong(), any(), any())).thenReturn(List.of());
        when(incomeRepository.findAllByUserIdAndRecebidaFalseAndDataBetween(anyLong(), any(), any())).thenReturn(List.of());
        when(subscriptionRepository.findAllByUserIdAndAtivaTrue(1L)).thenReturn(List.of());

        FluxoCaixaResponseDTO result = fluxoCaixaService.getFluxoCaixa(30);

        assertThat(result.saldoInicial()).isEqualByComparingTo("1000");
        assertThat(result.serie().get(0).saldoProjetado()).isEqualByComparingTo("1000");
    }

    @Test
    @DisplayName("Despesa pendente reduz o saldo projetado na sua data")
    void deveNetarNegativoParaDespesaPendente() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(bankAccountService.getSaldoAtualTotal()).thenReturn(BigDecimal.ZERO);
        Category categoria = Category.builder().id(5L).nome("Moradia").build();
        Expense despesa = Expense.builder().id(1L).user(user).categoria(categoria).descricao("Aluguel")
                .valor(BigDecimal.valueOf(500)).data(hoje.plusDays(3)).paga(false).build();
        when(expenseRepository.findUpcomingUnpaid(anyLong(), any(), any())).thenReturn(List.of(despesa));
        when(incomeRepository.findAllByUserIdAndRecebidaFalseAndDataBetween(anyLong(), any(), any())).thenReturn(List.of());
        when(subscriptionRepository.findAllByUserIdAndAtivaTrue(1L)).thenReturn(List.of());

        FluxoCaixaResponseDTO result = fluxoCaixaService.getFluxoCaixa(30);

        FluxoCaixaPontoDTO ponto = result.serie().stream()
                .filter(p -> p.data().equals(hoje.plusDays(3)))
                .findFirst().orElseThrow();
        assertThat(ponto.saldoProjetado()).isEqualByComparingTo("-500");
        assertThat(result.detalhamento()).anyMatch(i ->
                i.tipo().equals("DESPESA_PENDENTE") && i.valor().compareTo(BigDecimal.valueOf(-500)) == 0);
    }

    @Test
    @DisplayName("Receita pendente aumenta o saldo projetado na sua data")
    void deveNetarPositivoParaReceitaPendente() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(bankAccountService.getSaldoAtualTotal()).thenReturn(BigDecimal.ZERO);
        when(expenseRepository.findUpcomingUnpaid(anyLong(), any(), any())).thenReturn(List.of());
        Category categoria = Category.builder().id(6L).nome("Salário").build();
        Income receita = Income.builder().id(2L).user(user).categoria(categoria).descricao("Salário")
                .valor(BigDecimal.valueOf(3000)).data(hoje.plusDays(5)).recebida(false).build();
        when(incomeRepository.findAllByUserIdAndRecebidaFalseAndDataBetween(anyLong(), any(), any())).thenReturn(List.of(receita));
        when(subscriptionRepository.findAllByUserIdAndAtivaTrue(1L)).thenReturn(List.of());

        FluxoCaixaResponseDTO result = fluxoCaixaService.getFluxoCaixa(30);

        FluxoCaixaPontoDTO ponto = result.serie().stream()
                .filter(p -> p.data().equals(hoje.plusDays(5)))
                .findFirst().orElseThrow();
        assertThat(ponto.saldoProjetado()).isEqualByComparingTo("3000");
    }

    @Test
    @DisplayName("Cobrança de assinatura ainda não materializada aparece uma vez no detalhamento")
    void deveIncluirAssinaturaProjetadaQuandoAindaNaoGerada() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(bankAccountService.getSaldoAtualTotal()).thenReturn(BigDecimal.ZERO);
        when(expenseRepository.findUpcomingUnpaid(anyLong(), any(), any())).thenReturn(List.of());
        when(incomeRepository.findAllByUserIdAndRecebidaFalseAndDataBetween(anyLong(), any(), any())).thenReturn(List.of());

        Subscription assinatura = Subscription.builder().id(9L).user(user).descricao("Netflix")
                .valor(BigDecimal.valueOf(39.90)).diaCobranca(hoje.getDayOfMonth())
                .dataInicio(hoje.minusMonths(2)).ativa(true).build();
        when(subscriptionRepository.findAllByUserIdAndAtivaTrue(1L)).thenReturn(List.of(assinatura));
        when(expenseRepository.existsByAssinaturaIdAndReferenciaMes(eq(9L), any(LocalDate.class))).thenReturn(false);

        FluxoCaixaResponseDTO result = fluxoCaixaService.getFluxoCaixa(10);

        long count = result.detalhamento().stream().filter(i -> i.tipo().equals("ASSINATURA_PROJETADA")).count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Cobrança de assinatura já materializada não aparece duplicada no detalhamento")
    void deveExcluirAssinaturaJaGerada() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(bankAccountService.getSaldoAtualTotal()).thenReturn(BigDecimal.ZERO);
        when(expenseRepository.findUpcomingUnpaid(anyLong(), any(), any())).thenReturn(List.of());
        when(incomeRepository.findAllByUserIdAndRecebidaFalseAndDataBetween(anyLong(), any(), any())).thenReturn(List.of());

        Subscription assinatura = Subscription.builder().id(9L).user(user).descricao("Netflix")
                .valor(BigDecimal.valueOf(39.90)).diaCobranca(hoje.getDayOfMonth())
                .dataInicio(hoje.minusMonths(2)).ativa(true).build();
        when(subscriptionRepository.findAllByUserIdAndAtivaTrue(1L)).thenReturn(List.of(assinatura));
        when(expenseRepository.existsByAssinaturaIdAndReferenciaMes(eq(9L), any(LocalDate.class))).thenReturn(true);

        FluxoCaixaResponseDTO result = fluxoCaixaService.getFluxoCaixa(10);

        assertThat(result.detalhamento()).noneMatch(i -> i.tipo().equals("ASSINATURA_PROJETADA"));
    }

}
