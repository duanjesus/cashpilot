package com.cashpilot.service.impl;

import com.cashpilot.dto.request.ConectarOpenFinanceRequestDTO;
import com.cashpilot.dto.response.BankAccountResponseDTO;
import com.cashpilot.dto.response.CreditCardResponseDTO;
import com.cashpilot.dto.response.InstituicaoMockDTO;
import com.cashpilot.entity.BankAccount;
import com.cashpilot.entity.CreditCard;
import com.cashpilot.entity.User;
import com.cashpilot.entity.enums.BankAccountType;
import com.cashpilot.entity.enums.CardBrand;
import com.cashpilot.entity.enums.ContaOrigem;
import com.cashpilot.exception.BusinessException;
import com.cashpilot.exception.ResourceNotFoundException;
import com.cashpilot.repository.BankAccountRepository;
import com.cashpilot.repository.CreditCardRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.BankAccountService;
import com.cashpilot.service.CreditCardService;
import com.cashpilot.service.OpenFinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Stub implementation: connecting a mock institution creates a real
 * {@link BankAccount}/{@link CreditCard} row (so the rest of the app treats it
 * like any other account), and "syncing" only bumps {@code ultimaSincronizacao}.
 * No real bank API is ever called and no transaction data is ever fabricated.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OpenFinanceServiceImpl implements OpenFinanceService {

    private static final List<InstituicaoMockDTO> INSTITUICOES_MOCK = List.of(
            new InstituicaoMockDTO("banco-azul", "Banco Azul"),
            new InstituicaoMockDTO("banco-verde", "Banco Verde"),
            new InstituicaoMockDTO("carteira-digital-xp", "Carteira Digital XP"),
            new InstituicaoMockDTO("banco-cooperativo", "Banco Cooperativo")
    );

    private final BankAccountRepository bankAccountRepository;
    private final CreditCardRepository creditCardRepository;
    private final BankAccountService bankAccountService;
    private final CreditCardService creditCardService;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public List<InstituicaoMockDTO> listarInstituicoes() {
        return INSTITUICOES_MOCK;
    }

    @Override
    public BankAccountResponseDTO conectarConta(ConectarOpenFinanceRequestDTO dto) {
        currentUserProvider.requireWriteAccess();
        User user = currentUserProvider.getCurrentUser();

        BankAccount account = BankAccount.builder()
                .user(user)
                .nome(dto.apelido())
                .instituicao(dto.instituicaoNome())
                .tipo(BankAccountType.OUTRA)
                .saldoInicial(BigDecimal.ZERO)
                .dataSaldoInicial(LocalDate.now())
                .ativa(true)
                .origem(ContaOrigem.OPEN_FINANCE)
                .instituicaoNome(dto.instituicaoNome())
                .ultimaSincronizacao(LocalDateTime.now())
                .build();

        BankAccount saved = bankAccountRepository.save(account);
        return bankAccountService.findById(saved.getId());
    }

    @Override
    public CreditCardResponseDTO conectarCartao(ConectarOpenFinanceRequestDTO dto) {
        currentUserProvider.requireWriteAccess();
        User user = currentUserProvider.getCurrentUser();

        CreditCard card = CreditCard.builder()
                .user(user)
                .nome(dto.apelido())
                .bandeira(CardBrand.OUTRA)
                .limite(BigDecimal.ZERO)
                .diaFechamento(1)
                .diaVencimento(10)
                .ativo(true)
                .origem(ContaOrigem.OPEN_FINANCE)
                .instituicaoNome(dto.instituicaoNome())
                .ultimaSincronizacao(LocalDateTime.now())
                .build();

        CreditCard saved = creditCardRepository.save(card);
        return creditCardService.findById(saved.getId());
    }

    @Override
    public BankAccountResponseDTO sincronizarConta(Long id) {
        currentUserProvider.requireWriteAccess();
        BankAccount account = bankAccountRepository.findByIdAndUserIdIn(id, currentUserProvider.getScopeUserIds())
                .orElseThrow(() -> ResourceNotFoundException.of("Conta bancária", id));

        if (account.getOrigem() != ContaOrigem.OPEN_FINANCE) {
            throw new BusinessException("Esta conta não está conectada via Open Finance");
        }

        account.setUltimaSincronizacao(LocalDateTime.now());
        bankAccountRepository.save(account);
        return bankAccountService.findById(account.getId());
    }

    @Override
    public CreditCardResponseDTO sincronizarCartao(Long id) {
        currentUserProvider.requireWriteAccess();
        CreditCard card = creditCardRepository.findByIdAndUserIdIn(id, currentUserProvider.getScopeUserIds())
                .orElseThrow(() -> ResourceNotFoundException.of("Cartão de crédito", id));

        if (card.getOrigem() != ContaOrigem.OPEN_FINANCE) {
            throw new BusinessException("Este cartão não está conectado via Open Finance");
        }

        card.setUltimaSincronizacao(LocalDateTime.now());
        creditCardRepository.save(card);
        return creditCardService.findById(card.getId());
    }

}
