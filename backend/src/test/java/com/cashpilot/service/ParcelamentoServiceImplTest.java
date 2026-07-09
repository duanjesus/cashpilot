package com.cashpilot.service;

import com.cashpilot.dto.request.ParcelamentoRequestDTO;
import com.cashpilot.dto.response.ParcelamentoResponseDTO;
import com.cashpilot.entity.BankAccount;
import com.cashpilot.entity.Category;
import com.cashpilot.entity.Expense;
import com.cashpilot.entity.Parcelamento;
import com.cashpilot.entity.User;
import com.cashpilot.entity.enums.CategoryType;
import com.cashpilot.exception.BusinessException;
import com.cashpilot.mapper.ParcelamentoMapper;
import com.cashpilot.repository.BankAccountRepository;
import com.cashpilot.repository.CategoryRepository;
import com.cashpilot.repository.CreditCardRepository;
import com.cashpilot.repository.ExpenseRepository;
import com.cashpilot.repository.ParcelamentoRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.impl.ParcelamentoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParcelamentoService")
class ParcelamentoServiceImplTest {

    @Mock
    private ParcelamentoRepository parcelamentoRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private CreditCardRepository creditCardRepository;

    @Mock
    private ParcelamentoMapper parcelamentoMapper;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private ParcelamentoServiceImpl parcelamentoService;

    private User user;
    private Category categoria;
    private BankAccount conta;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Ana").email("ana@cashpilot.com").password("hash").build();
        categoria = Category.builder().id(2L).nome("Eletrônicos").tipo(CategoryType.DESPESA).build();
        conta = BankAccount.builder().id(3L).user(user).nome("Conta Corrente").build();
    }

    @Test
    @DisplayName("Deve dividir o valor total em parcelas cuja soma é exatamente igual ao valor total, com o resto na última parcela")
    void deveDividirValorTotalComRestoNaUltimaParcela() {
        ParcelamentoRequestDTO dto = new ParcelamentoRequestDTO(2L, 3L, null, "Notebook",
                BigDecimal.valueOf(100), 3, LocalDate.of(2026, 1, 10), null);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(categoria));
        when(bankAccountRepository.findByIdAndUserId(3L, 1L)).thenReturn(Optional.of(conta));
        when(parcelamentoRepository.save(any(Parcelamento.class))).thenAnswer(inv -> {
            Parcelamento p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });
        when(parcelamentoMapper.toResponseDto(any(Parcelamento.class))).thenReturn(
                new ParcelamentoResponseDTO(10L, "Notebook", BigDecimal.valueOf(100), 3, LocalDate.of(2026, 1, 10),
                        null, 2L, "Eletrônicos", 3L, "Conta Corrente", null, null, null, null, null, null));

        parcelamentoService.create(dto);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Expense>> captor = ArgumentCaptor.forClass(List.class);
        verify(expenseRepository).saveAll(captor.capture());
        List<Expense> parcelas = captor.getValue();

        assertThat(parcelas).hasSize(3);
        BigDecimal soma = parcelas.stream().map(Expense::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(soma).isEqualByComparingTo("100.00");
        assertThat(parcelas.get(0).getValor()).isEqualByComparingTo("33.33");
        assertThat(parcelas.get(1).getValor()).isEqualByComparingTo("33.33");
        assertThat(parcelas.get(2).getValor()).isEqualByComparingTo("33.34");
        assertThat(parcelas).allMatch(e -> !e.getPaga());
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao excluir parcelamento com parcelas já pagas")
    void deveLancarExcecaoAoExcluirComParcelasPagas() {
        Parcelamento parcelamento = Parcelamento.builder().id(20L).user(user).categoria(categoria)
                .contaBancaria(conta).descricao("Notebook").valorTotal(BigDecimal.valueOf(100))
                .numeroParcelas(3).dataPrimeiraParcela(LocalDate.of(2026, 1, 10)).build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(parcelamentoRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.of(parcelamento));
        when(expenseRepository.existsByParcelamentoIdAndPagaTrue(20L)).thenReturn(true);

        assertThatThrownBy(() -> parcelamentoService.delete(20L))
                .isInstanceOf(BusinessException.class);

        verify(parcelamentoRepository, never()).delete(any(Parcelamento.class));
    }

    @Test
    @DisplayName("Deve excluir despesas vinculadas e o parcelamento quando nenhuma parcela foi paga")
    void deveExcluirParcelasEParcelamentoQuandoNaoPago() {
        Parcelamento parcelamento = Parcelamento.builder().id(21L).user(user).categoria(categoria)
                .contaBancaria(conta).descricao("Notebook").valorTotal(BigDecimal.valueOf(100))
                .numeroParcelas(3).dataPrimeiraParcela(LocalDate.of(2026, 1, 10)).build();
        List<Expense> parcelas = List.of(
                Expense.builder().id(30L).parcelamento(parcelamento).paga(false).valor(BigDecimal.valueOf(33.33)).build(),
                Expense.builder().id(31L).parcelamento(parcelamento).paga(false).valor(BigDecimal.valueOf(33.33)).build(),
                Expense.builder().id(32L).parcelamento(parcelamento).paga(false).valor(BigDecimal.valueOf(33.34)).build()
        );

        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(parcelamentoRepository.findByIdAndUserId(21L, 1L)).thenReturn(Optional.of(parcelamento));
        when(expenseRepository.existsByParcelamentoIdAndPagaTrue(21L)).thenReturn(false);
        when(expenseRepository.findAllByParcelamentoId(21L)).thenReturn(parcelas);

        parcelamentoService.delete(21L);

        verify(expenseRepository).deleteAll(parcelas);
        verify(parcelamentoRepository).delete(parcelamento);
    }

}
