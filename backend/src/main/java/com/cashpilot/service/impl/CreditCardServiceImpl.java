package com.cashpilot.service.impl;

import com.cashpilot.dto.request.CreditCardRequestDTO;
import com.cashpilot.dto.response.CreditCardResponseDTO;
import com.cashpilot.entity.BankAccount;
import com.cashpilot.entity.CreditCard;
import com.cashpilot.entity.User;
import com.cashpilot.entity.enums.ContaOrigem;
import com.cashpilot.exception.BusinessException;
import com.cashpilot.exception.ResourceNotFoundException;
import com.cashpilot.mapper.CreditCardMapper;
import com.cashpilot.repository.BankAccountRepository;
import com.cashpilot.repository.CreditCardRepository;
import com.cashpilot.repository.ExpenseRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.CreditCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CreditCardServiceImpl implements CreditCardService {

    private final CreditCardRepository creditCardRepository;
    private final BankAccountRepository bankAccountRepository;
    private final ExpenseRepository expenseRepository;
    private final CreditCardMapper creditCardMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public CreditCardResponseDTO create(CreditCardRequestDTO dto) {
        currentUserProvider.requireWriteAccess();
        User user = currentUserProvider.getCurrentUser();
        BankAccount contaVinculada = resolveContaVinculada(dto.contaVinculadaId(), user.getId());

        CreditCard card = CreditCard.builder()
                .user(user)
                .nome(dto.nome())
                .bandeira(dto.bandeira())
                .limite(dto.limite())
                .diaFechamento(dto.diaFechamento())
                .diaVencimento(dto.diaVencimento())
                .contaVinculada(contaVinculada)
                .ativo(dto.ativo() == null || dto.ativo())
                .origem(dto.origem() == null ? ContaOrigem.MANUAL : dto.origem())
                .instituicaoNome(dto.instituicaoNome())
                .build();

        CreditCard saved = creditCardRepository.save(card);
        return toResponseWithFatura(saved);
    }

    @Override
    public CreditCardResponseDTO update(Long id, CreditCardRequestDTO dto) {
        currentUserProvider.requireWriteAccess();
        CreditCard card = findOwnedEntityById(id);
        BankAccount contaVinculada = resolveContaVinculada(dto.contaVinculadaId(), card.getUser().getId());

        card.setNome(dto.nome());
        card.setBandeira(dto.bandeira());
        card.setLimite(dto.limite());
        card.setDiaFechamento(dto.diaFechamento());
        card.setDiaVencimento(dto.diaVencimento());
        card.setContaVinculada(contaVinculada);
        if (dto.ativo() != null) {
            card.setAtivo(dto.ativo());
        }
        if (dto.origem() != null) {
            card.setOrigem(dto.origem());
        }
        card.setInstituicaoNome(dto.instituicaoNome());

        CreditCard updated = creditCardRepository.save(card);
        return toResponseWithFatura(updated);
    }

    @Override
    public void delete(Long id) {
        currentUserProvider.requireWriteAccess();
        CreditCard card = findOwnedEntityById(id);

        if (expenseRepository.existsByCartaoCreditoId(id)) {
            throw new BusinessException("Não é possível excluir um cartão de crédito em uso por despesas");
        }

        creditCardRepository.delete(card);
    }

    @Override
    @Transactional(readOnly = true)
    public CreditCardResponseDTO findById(Long id) {
        CreditCard card = findOwnedEntityById(id);
        return toResponseWithFatura(card);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditCardResponseDTO> findAll() {
        List<Long> scopeUserIds = currentUserProvider.getScopeUserIds();
        return creditCardRepository.findAllByUserIdIn(scopeUserIds).stream()
                .map(this::toResponseWithFatura)
                .toList();
    }

    private CreditCardResponseDTO toResponseWithFatura(CreditCard card) {
        CreditCardResponseDTO base = creditCardMapper.toResponseDto(card);
        BigDecimal faturaAtual = expenseRepository.sumValorByCartaoCreditoIdAndPagaFalse(card.getId());
        return new CreditCardResponseDTO(
                base.id(),
                base.nome(),
                base.bandeira(),
                base.limite(),
                base.diaFechamento(),
                base.diaVencimento(),
                base.contaVinculadaId(),
                base.contaVinculadaNome(),
                base.ativo(),
                faturaAtual,
                base.origem(),
                base.instituicaoNome(),
                base.ultimaSincronizacao()
        );
    }

    private BankAccount resolveContaVinculada(Long contaVinculadaId, Long userId) {
        if (contaVinculadaId == null) {
            return null;
        }
        return bankAccountRepository.findByIdAndUserIdIn(contaVinculadaId, currentUserProvider.getScopeUserIds())
                .orElseThrow(() -> ResourceNotFoundException.of("Conta bancária", contaVinculadaId));
    }

    private CreditCard findOwnedEntityById(Long id) {
        List<Long> scopeUserIds = currentUserProvider.getScopeUserIds();
        return creditCardRepository.findByIdAndUserIdIn(id, scopeUserIds)
                .orElseThrow(() -> ResourceNotFoundException.of("Cartão de crédito", id));
    }

}
