package com.cashpilot.service.impl;

import com.cashpilot.dto.request.BankAccountRequestDTO;
import com.cashpilot.dto.response.BankAccountResponseDTO;
import com.cashpilot.entity.BankAccount;
import com.cashpilot.entity.User;
import com.cashpilot.entity.enums.ContaOrigem;
import com.cashpilot.exception.BusinessException;
import com.cashpilot.exception.ResourceNotFoundException;
import com.cashpilot.mapper.BankAccountMapper;
import com.cashpilot.repository.BankAccountRepository;
import com.cashpilot.repository.ExpenseRepository;
import com.cashpilot.repository.IncomeRepository;
import com.cashpilot.repository.TransferRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final TransferRepository transferRepository;
    private final BankAccountMapper bankAccountMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public BankAccountResponseDTO create(BankAccountRequestDTO dto) {
        currentUserProvider.requireWriteAccess();
        User user = currentUserProvider.getCurrentUser();

        BankAccount account = BankAccount.builder()
                .user(user)
                .nome(dto.nome())
                .instituicao(dto.instituicao())
                .tipo(dto.tipo())
                .saldoInicial(dto.saldoInicial())
                .dataSaldoInicial(dto.dataSaldoInicial())
                .ativa(dto.ativa() == null || dto.ativa())
                .origem(dto.origem() == null ? ContaOrigem.MANUAL : dto.origem())
                .instituicaoNome(dto.instituicaoNome())
                .build();

        BankAccount saved = bankAccountRepository.save(account);
        return toResponseWithSaldo(saved);
    }

    @Override
    public BankAccountResponseDTO update(Long id, BankAccountRequestDTO dto) {
        currentUserProvider.requireWriteAccess();
        BankAccount account = findOwnedEntityById(id);

        account.setNome(dto.nome());
        account.setInstituicao(dto.instituicao());
        account.setTipo(dto.tipo());
        account.setSaldoInicial(dto.saldoInicial());
        account.setDataSaldoInicial(dto.dataSaldoInicial());
        if (dto.ativa() != null) {
            account.setAtiva(dto.ativa());
        }
        if (dto.origem() != null) {
            account.setOrigem(dto.origem());
        }
        account.setInstituicaoNome(dto.instituicaoNome());

        BankAccount updated = bankAccountRepository.save(account);
        return toResponseWithSaldo(updated);
    }

    @Override
    public void delete(Long id) {
        currentUserProvider.requireWriteAccess();
        BankAccount account = findOwnedEntityById(id);

        boolean inUse = incomeRepository.existsByContaBancariaId(id)
                || expenseRepository.existsByContaBancariaId(id)
                || transferRepository.existsByContaOrigemId(id)
                || transferRepository.existsByContaDestinoId(id);
        if (inUse) {
            throw new BusinessException("Não é possível excluir uma conta bancária em uso por receitas, despesas ou transferências");
        }

        bankAccountRepository.delete(account);
    }

    @Override
    @Transactional(readOnly = true)
    public BankAccountResponseDTO findById(Long id) {
        BankAccount account = findOwnedEntityById(id);
        return toResponseWithSaldo(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountResponseDTO> findAll() {
        List<Long> scopeUserIds = currentUserProvider.getScopeUserIds();
        return bankAccountRepository.findAllByUserIdIn(scopeUserIds).stream()
                .map(this::toResponseWithSaldo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getSaldoAtualTotal() {
        List<Long> scopeUserIds = currentUserProvider.getScopeUserIds();
        return bankAccountRepository.findAllByUserIdIn(scopeUserIds).stream()
                .filter(BankAccount::getAtiva)
                .map(this::computeSaldoAtual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Package-private: reused by {@link #getSaldoAtualTotal()} and available to other services in this package. */
    BigDecimal computeSaldoAtual(BankAccount account) {
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

    private BankAccountResponseDTO toResponseWithSaldo(BankAccount account) {
        BankAccountResponseDTO base = bankAccountMapper.toResponseDto(account);
        BigDecimal saldoAtual = computeSaldoAtual(account);
        return new BankAccountResponseDTO(
                base.id(),
                base.nome(),
                base.instituicao(),
                base.tipo(),
                base.saldoInicial(),
                base.dataSaldoInicial(),
                base.ativa(),
                saldoAtual,
                base.origem(),
                base.instituicaoNome(),
                base.ultimaSincronizacao()
        );
    }

    private BankAccount findOwnedEntityById(Long id) {
        List<Long> scopeUserIds = currentUserProvider.getScopeUserIds();
        return bankAccountRepository.findByIdAndUserIdIn(id, scopeUserIds)
                .orElseThrow(() -> ResourceNotFoundException.of("Conta bancária", id));
    }

}
