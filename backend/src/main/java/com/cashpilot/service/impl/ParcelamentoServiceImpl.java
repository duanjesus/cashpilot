package com.cashpilot.service.impl;

import com.cashpilot.dto.request.ParcelamentoRequestDTO;
import com.cashpilot.dto.response.ParcelamentoResponseDTO;
import com.cashpilot.entity.BankAccount;
import com.cashpilot.entity.Category;
import com.cashpilot.entity.CreditCard;
import com.cashpilot.entity.Expense;
import com.cashpilot.entity.Parcelamento;
import com.cashpilot.entity.User;
import com.cashpilot.entity.enums.CategoryType;
import com.cashpilot.exception.BusinessException;
import com.cashpilot.exception.ResourceNotFoundException;
import com.cashpilot.mapper.ParcelamentoMapper;
import com.cashpilot.repository.BankAccountRepository;
import com.cashpilot.repository.CategoryRepository;
import com.cashpilot.repository.CreditCardRepository;
import com.cashpilot.repository.ExpenseRepository;
import com.cashpilot.repository.ParcelamentoRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.ParcelamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ParcelamentoServiceImpl implements ParcelamentoService {

    private final ParcelamentoRepository parcelamentoRepository;
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final BankAccountRepository bankAccountRepository;
    private final CreditCardRepository creditCardRepository;
    private final ParcelamentoMapper parcelamentoMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public ParcelamentoResponseDTO create(ParcelamentoRequestDTO dto) {
        User user = currentUserProvider.getCurrentUser();
        validateFormaPagamento(dto.contaBancariaId(), dto.cartaoCreditoId());

        Category categoria = resolveCategoria(dto.categoriaId(), user.getId());
        BankAccount contaBancaria = resolveContaBancaria(dto.contaBancariaId(), user.getId());
        CreditCard cartaoCredito = resolveCartaoCredito(dto.cartaoCreditoId(), user.getId());

        Parcelamento parcelamento = Parcelamento.builder()
                .user(user)
                .categoria(categoria)
                .contaBancaria(contaBancaria)
                .cartaoCredito(cartaoCredito)
                .descricao(dto.descricao())
                .valorTotal(dto.valorTotal())
                .numeroParcelas(dto.numeroParcelas())
                .dataPrimeiraParcela(dto.dataPrimeiraParcela())
                .observacoes(dto.observacoes())
                .build();

        Parcelamento saved = parcelamentoRepository.save(parcelamento);

        List<Expense> parcelas = gerarParcelas(saved);
        expenseRepository.saveAll(parcelas);

        return toResponseWithProgresso(saved, parcelas);
    }

    @Override
    public void delete(Long id) {
        Parcelamento parcelamento = findOwnedEntityById(id);

        if (expenseRepository.existsByParcelamentoIdAndPagaTrue(id)) {
            throw new BusinessException("Não é possível excluir um parcelamento com parcelas já pagas");
        }

        expenseRepository.deleteAll(expenseRepository.findAllByParcelamentoId(id));
        parcelamentoRepository.delete(parcelamento);
    }

    @Override
    @Transactional(readOnly = true)
    public ParcelamentoResponseDTO findById(Long id) {
        Parcelamento parcelamento = findOwnedEntityById(id);
        List<Expense> parcelas = expenseRepository.findAllByParcelamentoId(id);
        return toResponseWithProgresso(parcelamento, parcelas);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ParcelamentoResponseDTO> findAll(Pageable pageable) {
        Long userId = currentUserProvider.getCurrentUserId();
        return parcelamentoRepository.findAllByUserId(userId, pageable)
                .map(p -> toResponseWithProgresso(p, expenseRepository.findAllByParcelamentoId(p.getId())));
    }

    /**
     * Splits {@code valorTotal} evenly across the installments via integer-cents division,
     * putting any rounding remainder into the last installment so the sum always equals
     * {@code valorTotal} exactly.
     */
    private List<Expense> gerarParcelas(Parcelamento parcelamento) {
        int numeroParcelas = parcelamento.getNumeroParcelas();
        BigDecimal valorTotal = parcelamento.getValorTotal();
        BigDecimal porParcela = valorTotal.divide(BigDecimal.valueOf(numeroParcelas), 2, RoundingMode.DOWN);
        BigDecimal ultimaParcela = valorTotal.subtract(porParcela.multiply(BigDecimal.valueOf(numeroParcelas - 1)));

        List<Expense> parcelas = new ArrayList<>();
        for (int i = 0; i < numeroParcelas; i++) {
            boolean isUltima = i == numeroParcelas - 1;
            Expense expense = Expense.builder()
                    .user(parcelamento.getUser())
                    .categoria(parcelamento.getCategoria())
                    .contaBancaria(parcelamento.getContaBancaria())
                    .cartaoCredito(parcelamento.getCartaoCredito())
                    .descricao(parcelamento.getDescricao() + " (" + (i + 1) + "/" + numeroParcelas + ")")
                    .valor(isUltima ? ultimaParcela : porParcela)
                    .data(parcelamento.getDataPrimeiraParcela().plusMonths(i))
                    .numeroParcela(i + 1)
                    .paga(false)
                    .parcelamento(parcelamento)
                    .build();
            parcelas.add(expense);
        }
        return parcelas;
    }

    private ParcelamentoResponseDTO toResponseWithProgresso(Parcelamento parcelamento, List<Expense> parcelas) {
        ParcelamentoResponseDTO base = parcelamentoMapper.toResponseDto(parcelamento);

        int parcelasPagas = (int) parcelas.stream().filter(Expense::getPaga).count();
        BigDecimal valorPago = parcelas.stream()
                .filter(Expense::getPaga)
                .map(Expense::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorRestante = parcelamento.getValorTotal().subtract(valorPago);
        boolean quitado = parcelasPagas == parcelamento.getNumeroParcelas();

        return new ParcelamentoResponseDTO(
                base.id(),
                base.descricao(),
                base.valorTotal(),
                base.numeroParcelas(),
                base.dataPrimeiraParcela(),
                base.observacoes(),
                base.categoriaId(),
                base.categoriaNome(),
                base.contaBancariaId(),
                base.contaBancariaNome(),
                base.cartaoCreditoId(),
                base.cartaoCreditoNome(),
                parcelasPagas,
                valorPago,
                valorRestante,
                quitado
        );
    }

    private void validateFormaPagamento(Long contaBancariaId, Long cartaoCreditoId) {
        boolean temConta = contaBancariaId != null;
        boolean temCartao = cartaoCreditoId != null;
        if (temConta == temCartao) {
            throw new BusinessException("Informe exatamente uma forma de pagamento: conta bancária ou cartão de crédito");
        }
    }

    private Category resolveCategoria(Long categoriaId, Long userId) {
        Category categoria = categoryRepository.findById(categoriaId)
                .filter(c -> c.getUser() == null || c.getUser().getId().equals(userId))
                .orElseThrow(() -> ResourceNotFoundException.of("Categoria", categoriaId));

        if (categoria.getTipo() != CategoryType.DESPESA && categoria.getTipo() != CategoryType.AMBOS) {
            throw new BusinessException("A categoria informada não é válida para parcelamentos");
        }
        return categoria;
    }

    private BankAccount resolveContaBancaria(Long contaId, Long userId) {
        if (contaId == null) {
            return null;
        }
        return bankAccountRepository.findByIdAndUserId(contaId, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Conta bancária", contaId));
    }

    private CreditCard resolveCartaoCredito(Long cartaoId, Long userId) {
        if (cartaoId == null) {
            return null;
        }
        return creditCardRepository.findByIdAndUserId(cartaoId, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Cartão de crédito", cartaoId));
    }

    private Parcelamento findOwnedEntityById(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        return parcelamentoRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Parcelamento", id));
    }

}
