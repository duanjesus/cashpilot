package com.cashpilot.service.impl;

import com.cashpilot.dto.request.IncomeRequestDTO;
import com.cashpilot.dto.request.MarkIncomeReceivedRequestDTO;
import com.cashpilot.dto.response.IncomeResponseDTO;
import com.cashpilot.entity.BankAccount;
import com.cashpilot.entity.Category;
import com.cashpilot.entity.Income;
import com.cashpilot.entity.User;
import com.cashpilot.entity.enums.CategoryType;
import com.cashpilot.exception.BusinessException;
import com.cashpilot.exception.ResourceNotFoundException;
import com.cashpilot.mapper.IncomeMapper;
import com.cashpilot.repository.BankAccountRepository;
import com.cashpilot.repository.CategoryRepository;
import com.cashpilot.repository.IncomeRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;
    private final BankAccountRepository bankAccountRepository;
    private final IncomeMapper incomeMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public IncomeResponseDTO create(IncomeRequestDTO dto) {
        User user = currentUserProvider.getCurrentUser();
        Category categoria = resolveCategoria(dto.categoriaId(), user.getId());
        BankAccount contaBancaria = resolveContaBancaria(dto.contaBancariaId(), user.getId());

        Income income = Income.builder()
                .user(user)
                .categoria(categoria)
                .contaBancaria(contaBancaria)
                .descricao(dto.descricao())
                .valor(dto.valor())
                .data(dto.data())
                .recorrente(dto.recorrente() != null && dto.recorrente())
                .recebida(dto.recebida() == null || dto.recebida())
                .dataRecebimento(dto.dataRecebimento())
                .observacoes(dto.observacoes())
                .build();

        Income saved = incomeRepository.save(income);
        return incomeMapper.toResponseDto(saved);
    }

    @Override
    public IncomeResponseDTO update(Long id, IncomeRequestDTO dto) {
        Income income = findOwnedEntityById(id);
        Category categoria = resolveCategoria(dto.categoriaId(), income.getUser().getId());
        BankAccount contaBancaria = resolveContaBancaria(dto.contaBancariaId(), income.getUser().getId());

        income.setCategoria(categoria);
        income.setContaBancaria(contaBancaria);
        income.setDescricao(dto.descricao());
        income.setValor(dto.valor());
        income.setData(dto.data());
        if (dto.recorrente() != null) {
            income.setRecorrente(dto.recorrente());
        }
        if (dto.recebida() != null) {
            income.setRecebida(dto.recebida());
        }
        income.setDataRecebimento(dto.dataRecebimento());
        income.setObservacoes(dto.observacoes());

        Income updated = incomeRepository.save(income);
        return incomeMapper.toResponseDto(updated);
    }

    @Override
    public void delete(Long id) {
        Income income = findOwnedEntityById(id);
        incomeRepository.delete(income);
    }

    @Override
    @Transactional(readOnly = true)
    public IncomeResponseDTO findById(Long id) {
        return incomeMapper.toResponseDto(findOwnedEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IncomeResponseDTO> findAll(LocalDate dataInicio, LocalDate dataFim, Long categoriaId, Long contaId, Boolean recebida, Pageable pageable) {
        Long userId = currentUserProvider.getCurrentUserId();
        return incomeRepository.findAllByFilters(userId, dataInicio, dataFim, categoriaId, contaId, recebida, pageable)
                .map(incomeMapper::toResponseDto);
    }

    @Override
    public IncomeResponseDTO markAsReceived(Long id, MarkIncomeReceivedRequestDTO dto) {
        Income income = findOwnedEntityById(id);
        income.setRecebida(true);
        income.setDataRecebimento(dto == null || dto.dataRecebimento() == null ? LocalDate.now() : dto.dataRecebimento());

        Income updated = incomeRepository.save(income);
        return incomeMapper.toResponseDto(updated);
    }

    private Category resolveCategoria(Long categoriaId, Long userId) {
        Category categoria = categoryRepository.findById(categoriaId)
                .filter(c -> c.getUser() == null || c.getUser().getId().equals(userId))
                .orElseThrow(() -> ResourceNotFoundException.of("Categoria", categoriaId));

        if (categoria.getTipo() != CategoryType.RECEITA && categoria.getTipo() != CategoryType.AMBOS) {
            throw new BusinessException("A categoria informada não é válida para receitas");
        }
        return categoria;
    }

    private BankAccount resolveContaBancaria(Long contaId, Long userId) {
        return bankAccountRepository.findByIdAndUserId(contaId, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Conta bancária", contaId));
    }

    private Income findOwnedEntityById(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        return incomeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Receita", id));
    }

}
