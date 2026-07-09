package com.cashpilot.service;

import com.cashpilot.dto.response.ExpenseResponseDTO;
import com.cashpilot.entity.BankAccount;
import com.cashpilot.entity.Category;
import com.cashpilot.entity.Expense;
import com.cashpilot.entity.Subscription;
import com.cashpilot.entity.User;
import com.cashpilot.mapper.ExpenseMapper;
import com.cashpilot.mapper.SubscriptionMapper;
import com.cashpilot.repository.BankAccountRepository;
import com.cashpilot.repository.CategoryRepository;
import com.cashpilot.repository.CreditCardRepository;
import com.cashpilot.repository.ExpenseRepository;
import com.cashpilot.repository.SubscriptionRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.impl.SubscriptionServiceImpl;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionService")
class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private CreditCardRepository creditCardRepository;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @Mock
    private ExpenseMapper expenseMapper;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private SubscriptionServiceImpl subscriptionService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Ana").email("ana@cashpilot.com").password("hash").build();
        // Real (non-mocked) SubscriptionChargeScheduler: this is the exact pure-math component
        // under test in SubscriptionChargeSchedulerTest, so exercising it here for real gives
        // confidence in the actual generated dates rather than a hand-picked stub.
        subscriptionService = new SubscriptionServiceImpl(
                subscriptionRepository, expenseRepository, categoryRepository, bankAccountRepository,
                creditCardRepository, subscriptionMapper, expenseMapper, new SubscriptionChargeScheduler(),
                currentUserProvider);
    }

    @Test
    @DisplayName("gerarPendentes() só processa assinaturas ativas do usuário autenticado")
    void deveProcessarApenasAssinaturasDoUsuarioAtual() {
        when(currentUserProvider.getScopeUserIds()).thenReturn(List.of(1L));
        when(subscriptionRepository.findAllByUserIdInAndAtivaTrue(List.of(1L))).thenReturn(List.of());

        List<ExpenseResponseDTO> result = subscriptionService.gerarPendentes();

        assertThat(result).isEmpty();
        verify(subscriptionRepository).findAllByUserIdInAndAtivaTrue(List.of(1L));
        verify(subscriptionRepository, never()).findAllByAtivaTrue();
    }

    @Test
    @DisplayName("Não gera cobrança duplicada quando já existe despesa para (assinaturaId, referenciaMes)")
    void deveIgnorarCobrancaJaGerada() {
        Subscription assinatura = subscription(LocalDate.of(2026, 6, 5), null, 5);
        LocalDate hoje = LocalDate.of(2026, 7, 5);

        when(expenseRepository.existsByAssinaturaIdAndReferenciaMes(eq(assinatura.getId()), any(LocalDate.class)))
                .thenReturn(true);

        List<ExpenseResponseDTO> result = subscriptionService.gerarCobrancasParaAssinatura(assinatura, hoje);

        assertThat(result).isEmpty();
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    @DisplayName("Gera o backlog completo de meses quando a data de início é anterior em vários meses")
    void deveGerarBacklogQuandoDataInicioAntiga() {
        Subscription assinatura = subscription(LocalDate.of(2026, 1, 5), null, 5);
        LocalDate hoje = LocalDate.of(2026, 4, 5);

        when(expenseRepository.existsByAssinaturaIdAndReferenciaMes(anyLong(), any(LocalDate.class))).thenReturn(false);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));
        when(expenseMapper.toResponseDto(any(Expense.class))).thenReturn(
                new ExpenseResponseDTO(1L, "Netflix", assinatura.getValor(), hoje, false, null, null,
                        null, null, null, null, null, null, null, null, assinatura.getId(), null));

        List<ExpenseResponseDTO> result = subscriptionService.gerarCobrancasParaAssinatura(assinatura, hoje);

        assertThat(result).hasSize(4);
        verify(expenseRepository, times(4)).save(any(Expense.class));
    }

    private Subscription subscription(LocalDate dataInicio, LocalDate dataFim, int diaCobranca) {
        Category categoria = Category.builder().id(2L).nome("Assinaturas").build();
        BankAccount conta = BankAccount.builder().id(3L).build();
        return Subscription.builder()
                .id(10L)
                .user(user)
                .categoria(categoria)
                .contaBancaria(conta)
                .descricao("Netflix")
                .valor(BigDecimal.valueOf(39.90))
                .diaCobranca(diaCobranca)
                .dataInicio(dataInicio)
                .dataFim(dataFim)
                .ativa(true)
                .build();
    }

}
