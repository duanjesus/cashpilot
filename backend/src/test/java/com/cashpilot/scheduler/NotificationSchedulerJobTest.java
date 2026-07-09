package com.cashpilot.scheduler;

import com.cashpilot.entity.CreditCard;
import com.cashpilot.entity.Expense;
import com.cashpilot.entity.FinancialGoal;
import com.cashpilot.entity.Notification;
import com.cashpilot.entity.User;
import com.cashpilot.entity.enums.GoalType;
import com.cashpilot.entity.enums.NotificationTipo;
import com.cashpilot.repository.CreditCardRepository;
import com.cashpilot.repository.ExpenseRepository;
import com.cashpilot.repository.FinancialGoalRepository;
import com.cashpilot.repository.IncomeRepository;
import com.cashpilot.repository.NotificationRepository;
import com.cashpilot.security.CurrentUserProvider;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationSchedulerJob")
class NotificationSchedulerJobTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private CreditCardRepository creditCardRepository;

    @Mock
    private FinancialGoalRepository financialGoalRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private NotificationSchedulerJob job;

    private User user;

    @BeforeEach
    void setUp() {
        job = new NotificationSchedulerJob(expenseRepository, incomeRepository, creditCardRepository,
                financialGoalRepository, notificationRepository, currentUserProvider);
        user = User.builder().id(1L).name("Ana").email("ana@cashpilot.com").password("hash").build();

        // Default: no despesas/receitas/cartões pending across passes not under test.
        when(expenseRepository.findAll()).thenReturn(List.of());
        when(incomeRepository.findAll()).thenReturn(List.of());
        when(creditCardRepository.findAll()).thenReturn(List.of());
        when(financialGoalRepository.findAll()).thenReturn(List.of());
    }

    @Test
    @DisplayName("Meta financeira com valorAtual >= valorAlvo gera notificação META_ATINGIDA")
    void deveNotificarMetaAtingida() {
        FinancialGoal goal = FinancialGoal.builder()
                .id(5L)
                .user(user)
                .nome("Reserva de emergência")
                .valorAlvo(BigDecimal.valueOf(1000))
                .valorAtual(BigDecimal.valueOf(1000))
                .dataAlvo(LocalDate.now().plusMonths(6))
                .dataInicio(LocalDate.now().minusMonths(1))
                .ativa(true)
                .tipo(GoalType.MANUAL)
                .build();
        when(financialGoalRepository.findAll()).thenReturn(List.of(goal));
        when(currentUserProvider.resolveScopeUserIds(1L)).thenReturn(List.of(1L));
        when(notificationRepository.existsByUserIdAndTipoAndReferenciaTipoAndReferenciaIdAndAnoMesReferencia(
                eq(1L), eq(NotificationTipo.META_ATINGIDA), eq("META"), eq(5L), eq(null)))
                .thenReturn(false);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Notification> geradas = job.gerarNotificacoesPendentes();

        assertThat(geradas).hasSize(1);
        assertThat(geradas.get(0).getTipo()).isEqualTo(NotificationTipo.META_ATINGIDA);
        assertThat(geradas.get(0).getReferenciaId()).isEqualTo(5L);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Meta financeira abaixo do valorAlvo não gera notificação")
    void naoDeveNotificarMetaAbaixoDoAlvo() {
        FinancialGoal goal = FinancialGoal.builder()
                .id(5L)
                .user(user)
                .nome("Reserva de emergência")
                .valorAlvo(BigDecimal.valueOf(1000))
                .valorAtual(BigDecimal.valueOf(500))
                .dataAlvo(LocalDate.now().plusMonths(6))
                .dataInicio(LocalDate.now().minusMonths(1))
                .ativa(true)
                .tipo(GoalType.MANUAL)
                .build();
        when(financialGoalRepository.findAll()).thenReturn(List.of(goal));

        List<Notification> geradas = job.gerarNotificacoesPendentes();

        assertThat(geradas).isEmpty();
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Rodar a geração duas vezes não duplica notificação (dedupe via existsBy...)")
    void naoDeveDuplicarNotificacaoJaGerada() {
        FinancialGoal goal = FinancialGoal.builder()
                .id(5L)
                .user(user)
                .nome("Reserva de emergência")
                .valorAlvo(BigDecimal.valueOf(1000))
                .valorAtual(BigDecimal.valueOf(1000))
                .dataAlvo(LocalDate.now().plusMonths(6))
                .dataInicio(LocalDate.now().minusMonths(1))
                .ativa(true)
                .tipo(GoalType.MANUAL)
                .build();
        when(financialGoalRepository.findAll()).thenReturn(List.of(goal));
        when(currentUserProvider.resolveScopeUserIds(1L)).thenReturn(List.of(1L));
        // Simulates: first run finds nothing yet, second run finds the row already created.
        when(notificationRepository.existsByUserIdAndTipoAndReferenciaTipoAndReferenciaIdAndAnoMesReferencia(
                eq(1L), eq(NotificationTipo.META_ATINGIDA), eq("META"), eq(5L), eq(null)))
                .thenReturn(false, true);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Notification> primeiraExecucao = job.gerarNotificacoesPendentes();
        List<Notification> segundaExecucao = job.gerarNotificacoesPendentes();

        assertThat(primeiraExecucao).hasSize(1);
        assertThat(segundaExecucao).isEmpty();
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Despesa não paga vencendo em até 3 dias gera notificação CONTA_A_VENCER")
    void deveNotificarDespesaAVencer() {
        Expense expense = Expense.builder()
                .id(9L)
                .user(user)
                .descricao("Aluguel")
                .valor(BigDecimal.valueOf(1500))
                .data(LocalDate.now().plusDays(2))
                .paga(false)
                .build();
        when(expenseRepository.findAll()).thenReturn(List.of(expense));
        when(currentUserProvider.resolveScopeUserIds(1L)).thenReturn(List.of(1L));
        when(notificationRepository.existsByUserIdAndTipoAndReferenciaTipoAndReferenciaIdAndAnoMesReferencia(
                eq(1L), eq(NotificationTipo.CONTA_A_VENCER), eq("DESPESA"), eq(9L), eq(null)))
                .thenReturn(false);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Notification> geradas = job.gerarNotificacoesPendentes();

        assertThat(geradas).hasSize(1);
        assertThat(geradas.get(0).getReferenciaTipo()).isEqualTo("DESPESA");
        assertThat(geradas.get(0).getReferenciaId()).isEqualTo(9L);
    }

}
