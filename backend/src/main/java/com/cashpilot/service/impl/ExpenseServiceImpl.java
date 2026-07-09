package com.cashpilot.service.impl;

import com.cashpilot.dto.request.ExpenseRequestDTO;
import com.cashpilot.dto.request.MarkExpensePaidRequestDTO;
import com.cashpilot.dto.response.ExpenseResponseDTO;
import com.cashpilot.entity.BankAccount;
import com.cashpilot.entity.Category;
import com.cashpilot.entity.CreditCard;
import com.cashpilot.entity.Expense;
import com.cashpilot.entity.User;
import com.cashpilot.entity.enums.CategoryType;
import com.cashpilot.exception.BusinessException;
import com.cashpilot.exception.ResourceNotFoundException;
import com.cashpilot.mapper.ExpenseMapper;
import com.cashpilot.repository.BankAccountRepository;
import com.cashpilot.repository.CategoryRepository;
import com.cashpilot.repository.CreditCardRepository;
import com.cashpilot.repository.ExpenseRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final BankAccountRepository bankAccountRepository;
    private final CreditCardRepository creditCardRepository;
    private final ExpenseMapper expenseMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public ExpenseResponseDTO create(ExpenseRequestDTO dto) {
        currentUserProvider.requireWriteAccess();
        User user = currentUserProvider.getCurrentUser();
        validateFormaPagamento(dto.contaBancariaId(), dto.cartaoCreditoId());

        Category categoria = resolveCategoria(dto.categoriaId(), user.getId());
        BankAccount contaBancaria = resolveContaBancaria(dto.contaBancariaId(), user.getId());
        CreditCard cartaoCredito = resolveCartaoCredito(dto.cartaoCreditoId(), user.getId());

        Expense expense = Expense.builder()
                .user(user)
                .categoria(categoria)
                .contaBancaria(contaBancaria)
                .cartaoCredito(cartaoCredito)
                .descricao(dto.descricao())
                .valor(dto.valor())
                .data(dto.data())
                .paga(dto.paga() == null || dto.paga())
                .dataPagamento(dto.dataPagamento())
                .observacoes(dto.observacoes())
                .build();

        Expense saved = expenseRepository.save(expense);
        return expenseMapper.toResponseDto(saved);
    }

    @Override
    public ExpenseResponseDTO update(Long id, ExpenseRequestDTO dto) {
        currentUserProvider.requireWriteAccess();
        Expense expense = findOwnedEntityById(id);
        validateFormaPagamento(dto.contaBancariaId(), dto.cartaoCreditoId());

        Long userId = expense.getUser().getId();
        Category categoria = resolveCategoria(dto.categoriaId(), userId);
        BankAccount contaBancaria = resolveContaBancaria(dto.contaBancariaId(), userId);
        CreditCard cartaoCredito = resolveCartaoCredito(dto.cartaoCreditoId(), userId);

        expense.setCategoria(categoria);
        expense.setContaBancaria(contaBancaria);
        expense.setCartaoCredito(cartaoCredito);
        expense.setDescricao(dto.descricao());
        expense.setValor(dto.valor());
        expense.setData(dto.data());
        if (dto.paga() != null) {
            expense.setPaga(dto.paga());
        }
        expense.setDataPagamento(dto.dataPagamento());
        expense.setObservacoes(dto.observacoes());

        Expense updated = expenseRepository.save(expense);
        return expenseMapper.toResponseDto(updated);
    }

    @Override
    public void delete(Long id) {
        currentUserProvider.requireWriteAccess();
        Expense expense = findOwnedEntityById(id);
        expenseRepository.delete(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponseDTO findById(Long id) {
        return expenseMapper.toResponseDto(findOwnedEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseResponseDTO> findAll(LocalDate dataInicio, LocalDate dataFim, Long categoriaId, Long contaId,
                                             Long cartaoId, Boolean paga, Pageable pageable) {
        List<Long> scopeUserIds = currentUserProvider.getScopeUserIds();
        return expenseRepository.findAllByFilters(scopeUserIds, dataInicio, dataFim, categoriaId, contaId, cartaoId, paga, pageable)
                .map(expenseMapper::toResponseDto);
    }

    @Override
    public ExpenseResponseDTO markAsPaid(Long id, MarkExpensePaidRequestDTO dto) {
        currentUserProvider.requireWriteAccess();
        Expense expense = findOwnedEntityById(id);
        expense.setPaga(true);
        expense.setDataPagamento(dto == null || dto.dataPagamento() == null ? LocalDate.now() : dto.dataPagamento());

        Expense updated = expenseRepository.save(expense);
        return expenseMapper.toResponseDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> findAllForExport(LocalDate dataInicio, LocalDate dataFim, Long categoriaId, Long contaId,
                                           Long cartaoId, Boolean paga) {
        List<Long> scopeUserIds = currentUserProvider.getScopeUserIds();
        return expenseRepository.findAllByFiltersList(scopeUserIds, dataInicio, dataFim, categoriaId, contaId, cartaoId, paga);
    }

    private void validateFormaPagamento(Long contaBancariaId, Long cartaoCreditoId) {
        boolean temConta = contaBancariaId != null;
        boolean temCartao = cartaoCreditoId != null;
        if (temConta == temCartao) {
            throw new BusinessException("Informe exatamente uma forma de pagamento: conta bancária ou cartão de crédito");
        }
    }

    private Category resolveCategoria(Long categoriaId, Long userId) {
        List<Long> scopeUserIds = currentUserProvider.getScopeUserIds();
        Category categoria = categoryRepository.findById(categoriaId)
                .filter(c -> c.getUser() == null || scopeUserIds.contains(c.getUser().getId()))
                .orElseThrow(() -> ResourceNotFoundException.of("Categoria", categoriaId));

        if (categoria.getTipo() != CategoryType.DESPESA && categoria.getTipo() != CategoryType.AMBOS) {
            throw new BusinessException("A categoria informada não é válida para despesas");
        }
        return categoria;
    }

    private BankAccount resolveContaBancaria(Long contaId, Long userId) {
        if (contaId == null) {
            return null;
        }
        return bankAccountRepository.findByIdAndUserIdIn(contaId, currentUserProvider.getScopeUserIds())
                .orElseThrow(() -> ResourceNotFoundException.of("Conta bancária", contaId));
    }

    private CreditCard resolveCartaoCredito(Long cartaoId, Long userId) {
        if (cartaoId == null) {
            return null;
        }
        return creditCardRepository.findByIdAndUserIdIn(cartaoId, currentUserProvider.getScopeUserIds())
                .orElseThrow(() -> ResourceNotFoundException.of("Cartão de crédito", cartaoId));
    }

    private Expense findOwnedEntityById(Long id) {
        List<Long> scopeUserIds = currentUserProvider.getScopeUserIds();
        return expenseRepository.findByIdAndUserIdIn(id, scopeUserIds)
                .orElseThrow(() -> ResourceNotFoundException.of("Despesa", id));
    }

}
