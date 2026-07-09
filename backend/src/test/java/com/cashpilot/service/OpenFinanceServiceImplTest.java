package com.cashpilot.service;

import com.cashpilot.dto.request.ConectarOpenFinanceRequestDTO;
import com.cashpilot.dto.response.BankAccountResponseDTO;
import com.cashpilot.dto.response.CreditCardResponseDTO;
import com.cashpilot.entity.BankAccount;
import com.cashpilot.entity.CreditCard;
import com.cashpilot.entity.User;
import com.cashpilot.entity.enums.BankAccountType;
import com.cashpilot.entity.enums.CardBrand;
import com.cashpilot.entity.enums.ContaOrigem;
import com.cashpilot.entity.enums.TipoConexao;
import com.cashpilot.exception.BusinessException;
import com.cashpilot.exception.ResourceNotFoundException;
import com.cashpilot.repository.BankAccountRepository;
import com.cashpilot.repository.CreditCardRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.impl.OpenFinanceServiceImpl;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenFinanceService")
class OpenFinanceServiceImplTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private CreditCardRepository creditCardRepository;

    @Mock
    private BankAccountService bankAccountService;

    @Mock
    private CreditCardService creditCardService;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private OpenFinanceServiceImpl openFinanceService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Ana").email("ana@cashpilot.com").password("hash").build();
    }

    @Test
    @DisplayName("Deve conectar uma conta mock criando um BankAccount com origem OPEN_FINANCE")
    void deveConectarContaComOrigemOpenFinance() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        BankAccount saved = BankAccount.builder()
                .id(20L)
                .user(user)
                .nome("Minha Conta Azul")
                .instituicao("Banco Azul")
                .tipo(BankAccountType.OUTRA)
                .saldoInicial(BigDecimal.ZERO)
                .dataSaldoInicial(LocalDate.now())
                .ativa(true)
                .origem(ContaOrigem.OPEN_FINANCE)
                .instituicaoNome("Banco Azul")
                .build();
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(saved);
        BankAccountResponseDTO expectedResponse = new BankAccountResponseDTO(
                20L, "Minha Conta Azul", "Banco Azul", BankAccountType.OUTRA,
                BigDecimal.ZERO, LocalDate.now(), true, BigDecimal.ZERO,
                ContaOrigem.OPEN_FINANCE, "Banco Azul", null);
        when(bankAccountService.findById(20L)).thenReturn(expectedResponse);

        ConectarOpenFinanceRequestDTO dto = new ConectarOpenFinanceRequestDTO("Banco Azul", TipoConexao.CONTA, "Minha Conta Azul");

        BankAccountResponseDTO response = openFinanceService.conectarConta(dto);

        assertThat(response.origem()).isEqualTo(ContaOrigem.OPEN_FINANCE);
        assertThat(response.instituicaoNome()).isEqualTo("Banco Azul");

        ArgumentCaptor<BankAccount> captor = ArgumentCaptor.forClass(BankAccount.class);
        verify(bankAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getOrigem()).isEqualTo(ContaOrigem.OPEN_FINANCE);
        assertThat(captor.getValue().getNome()).isEqualTo("Minha Conta Azul");
        assertThat(captor.getValue().getUltimaSincronizacao()).isNotNull();
    }

    @Test
    @DisplayName("Deve conectar um cartão mock criando um CreditCard com origem OPEN_FINANCE")
    void deveConectarCartaoComOrigemOpenFinance() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        CreditCard saved = CreditCard.builder()
                .id(30L)
                .user(user)
                .nome("Cartão Verde")
                .bandeira(CardBrand.OUTRA)
                .limite(BigDecimal.ZERO)
                .diaFechamento(1)
                .diaVencimento(10)
                .ativo(true)
                .origem(ContaOrigem.OPEN_FINANCE)
                .instituicaoNome("Banco Verde")
                .build();
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(saved);
        CreditCardResponseDTO expectedResponse = new CreditCardResponseDTO(
                30L, "Cartão Verde", CardBrand.OUTRA, BigDecimal.ZERO, 1, 10,
                null, null, true, BigDecimal.ZERO, ContaOrigem.OPEN_FINANCE, "Banco Verde", null);
        when(creditCardService.findById(30L)).thenReturn(expectedResponse);

        ConectarOpenFinanceRequestDTO dto = new ConectarOpenFinanceRequestDTO("Banco Verde", TipoConexao.CARTAO, "Cartão Verde");

        CreditCardResponseDTO response = openFinanceService.conectarCartao(dto);

        assertThat(response.origem()).isEqualTo(ContaOrigem.OPEN_FINANCE);
        verify(creditCardRepository).save(any(CreditCard.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao sincronizar conta que não é Open Finance")
    void deveLancarExcecaoAoSincronizarContaNaoOpenFinance() {
        BankAccount manualAccount = BankAccount.builder()
                .id(40L)
                .user(user)
                .nome("Conta Manual")
                .tipo(BankAccountType.CORRENTE)
                .saldoInicial(BigDecimal.ZERO)
                .dataSaldoInicial(LocalDate.now())
                .ativa(true)
                .origem(ContaOrigem.MANUAL)
                .build();
        when(currentUserProvider.getScopeUserIds()).thenReturn(List.of(1L));
        when(bankAccountRepository.findByIdAndUserIdIn(40L, List.of(1L))).thenReturn(Optional.of(manualAccount));

        assertThatThrownBy(() -> openFinanceService.sincronizarConta(40L))
                .isInstanceOf(BusinessException.class);

        verify(bankAccountRepository, org.mockito.Mockito.never()).save(any(BankAccount.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao sincronizar cartão que não é Open Finance")
    void deveLancarExcecaoAoSincronizarCartaoNaoOpenFinance() {
        CreditCard manualCard = CreditCard.builder()
                .id(50L)
                .user(user)
                .nome("Cartão Manual")
                .bandeira(CardBrand.VISA)
                .limite(BigDecimal.TEN)
                .diaFechamento(5)
                .diaVencimento(15)
                .ativo(true)
                .origem(ContaOrigem.MANUAL)
                .build();
        when(currentUserProvider.getScopeUserIds()).thenReturn(List.of(1L));
        when(creditCardRepository.findByIdAndUserIdIn(50L, List.of(1L))).thenReturn(Optional.of(manualCard));

        assertThatThrownBy(() -> openFinanceService.sincronizarCartao(50L))
                .isInstanceOf(BusinessException.class);

        verify(creditCardRepository, org.mockito.Mockito.never()).save(any(CreditCard.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao sincronizar conta inexistente")
    void deveLancarExcecaoQuandoContaNaoEncontrada() {
        when(currentUserProvider.getScopeUserIds()).thenReturn(List.of(1L));
        when(bankAccountRepository.findByIdAndUserIdIn(99L, List.of(1L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> openFinanceService.sincronizarConta(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

}
