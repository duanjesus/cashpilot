package com.cashpilot.service;

import com.cashpilot.dto.request.BankAccountRequestDTO;
import com.cashpilot.dto.response.BankAccountResponseDTO;
import com.cashpilot.entity.BankAccount;
import com.cashpilot.entity.User;
import com.cashpilot.entity.enums.BankAccountType;
import com.cashpilot.exception.BusinessException;
import com.cashpilot.exception.ResourceNotFoundException;
import com.cashpilot.mapper.BankAccountMapper;
import com.cashpilot.repository.BankAccountRepository;
import com.cashpilot.repository.ExpenseRepository;
import com.cashpilot.repository.IncomeRepository;
import com.cashpilot.repository.TransferRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.impl.BankAccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BankAccountService")
class BankAccountServiceImplTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private BankAccountMapper bankAccountMapper;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private BankAccountServiceImpl bankAccountService;

    private User user;
    private BankAccountRequestDTO requestDTO;
    private BankAccount account;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Ana").email("ana@cashpilot.com").password("hash").build();
        requestDTO = new BankAccountRequestDTO(
                "Conta Corrente", "Banco X", BankAccountType.CORRENTE,
                new BigDecimal("1000.00"), LocalDate.of(2026, 1, 1), true);
        account = BankAccount.builder()
                .id(10L)
                .user(user)
                .nome(requestDTO.nome())
                .instituicao(requestDTO.instituicao())
                .tipo(requestDTO.tipo())
                .saldoInicial(requestDTO.saldoInicial())
                .dataSaldoInicial(requestDTO.dataSaldoInicial())
                .ativa(true)
                .build();
    }

    @Test
    @DisplayName("Deve criar conta bancária com sucesso")
    void deveCriarContaComSucesso() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(account);
        when(bankAccountMapper.toResponseDto(account)).thenReturn(
                new BankAccountResponseDTO(10L, requestDTO.nome(), requestDTO.instituicao(), requestDTO.tipo(),
                        requestDTO.saldoInicial(), requestDTO.dataSaldoInicial(), true, null));
        when(incomeRepository.sumValorByContaBancariaId(10L)).thenReturn(BigDecimal.ZERO);
        when(expenseRepository.sumValorByContaBancariaId(10L)).thenReturn(BigDecimal.ZERO);
        when(transferRepository.sumValorByContaOrigemId(10L)).thenReturn(BigDecimal.ZERO);
        when(transferRepository.sumValorByContaDestinoId(10L)).thenReturn(BigDecimal.ZERO);

        BankAccountResponseDTO response = bankAccountService.create(requestDTO);

        assertThat(response).isNotNull();
        assertThat(response.nome()).isEqualTo("Conta Corrente");
        assertThat(response.saldoAtual()).isEqualByComparingTo(requestDTO.saldoInicial());
        verify(bankAccountRepository, times(1)).save(any(BankAccount.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao excluir conta referenciada por receitas")
    void deveLancarExcecaoAoExcluirContaReferenciada() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(bankAccountRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(account));
        when(incomeRepository.existsByContaBancariaId(10L)).thenReturn(true);

        assertThatThrownBy(() -> bankAccountService.delete(10L))
                .isInstanceOf(BusinessException.class);

        verify(bankAccountRepository, never()).delete(any(BankAccount.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar conta que não pertence ao usuário")
    void deveLancarExcecaoQuandoContaNaoPertenceAoUsuario() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(bankAccountRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankAccountService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

}
