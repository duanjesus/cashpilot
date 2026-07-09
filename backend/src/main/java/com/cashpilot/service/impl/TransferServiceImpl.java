package com.cashpilot.service.impl;

import com.cashpilot.dto.request.TransferRequestDTO;
import com.cashpilot.dto.response.TransferResponseDTO;
import com.cashpilot.entity.BankAccount;
import com.cashpilot.entity.Transfer;
import com.cashpilot.entity.User;
import com.cashpilot.exception.BusinessException;
import com.cashpilot.exception.ResourceNotFoundException;
import com.cashpilot.mapper.TransferMapper;
import com.cashpilot.repository.BankAccountRepository;
import com.cashpilot.repository.TransferRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransferMapper transferMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public TransferResponseDTO create(TransferRequestDTO dto) {
        currentUserProvider.requireWriteAccess();
        User user = currentUserProvider.getCurrentUser();

        if (dto.contaOrigemId().equals(dto.contaDestinoId())) {
            throw new BusinessException("A conta de origem e a conta de destino devem ser diferentes");
        }

        List<Long> scopeUserIds = currentUserProvider.getScopeUserIds();
        BankAccount contaOrigem = bankAccountRepository.findByIdAndUserIdIn(dto.contaOrigemId(), scopeUserIds)
                .orElseThrow(() -> ResourceNotFoundException.of("Conta bancária", dto.contaOrigemId()));
        BankAccount contaDestino = bankAccountRepository.findByIdAndUserIdIn(dto.contaDestinoId(), scopeUserIds)
                .orElseThrow(() -> ResourceNotFoundException.of("Conta bancária", dto.contaDestinoId()));

        Transfer transfer = Transfer.builder()
                .user(user)
                .contaOrigem(contaOrigem)
                .contaDestino(contaDestino)
                .valor(dto.valor())
                .data(dto.data())
                .descricao(dto.descricao())
                .build();

        Transfer saved = transferRepository.save(transfer);
        return transferMapper.toResponseDto(saved);
    }

    @Override
    public void delete(Long id) {
        currentUserProvider.requireWriteAccess();
        Transfer transfer = findOwnedEntityById(id);
        transferRepository.delete(transfer);
    }

    @Override
    @Transactional(readOnly = true)
    public TransferResponseDTO findById(Long id) {
        return transferMapper.toResponseDto(findOwnedEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransferResponseDTO> findAll(Pageable pageable) {
        List<Long> scopeUserIds = currentUserProvider.getScopeUserIds();
        return transferRepository.findAllByUserIdIn(scopeUserIds, pageable).map(transferMapper::toResponseDto);
    }

    private Transfer findOwnedEntityById(Long id) {
        List<Long> scopeUserIds = currentUserProvider.getScopeUserIds();
        return transferRepository.findByIdAndUserIdIn(id, scopeUserIds)
                .orElseThrow(() -> ResourceNotFoundException.of("Transferência", id));
    }

}
