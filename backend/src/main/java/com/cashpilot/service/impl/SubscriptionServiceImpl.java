package com.cashpilot.service.impl;

import com.cashpilot.dto.request.SubscriptionRequestDTO;
import com.cashpilot.dto.response.ExpenseResponseDTO;
import com.cashpilot.dto.response.SubscriptionResponseDTO;
import com.cashpilot.entity.BankAccount;
import com.cashpilot.entity.Category;
import com.cashpilot.entity.CreditCard;
import com.cashpilot.entity.Expense;
import com.cashpilot.entity.Subscription;
import com.cashpilot.entity.User;
import com.cashpilot.entity.enums.CategoryType;
import com.cashpilot.exception.BusinessException;
import com.cashpilot.exception.ResourceNotFoundException;
import com.cashpilot.mapper.ExpenseMapper;
import com.cashpilot.mapper.SubscriptionMapper;
import com.cashpilot.repository.BankAccountRepository;
import com.cashpilot.repository.CategoryRepository;
import com.cashpilot.repository.CreditCardRepository;
import com.cashpilot.repository.ExpenseRepository;
import com.cashpilot.repository.SubscriptionRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.SubscriptionChargeScheduler;
import com.cashpilot.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionServiceImpl.class);

    private final SubscriptionRepository subscriptionRepository;
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final BankAccountRepository bankAccountRepository;
    private final CreditCardRepository creditCardRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final ExpenseMapper expenseMapper;
    private final SubscriptionChargeScheduler subscriptionChargeScheduler;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public SubscriptionResponseDTO create(SubscriptionRequestDTO dto) {
        currentUserProvider.requireWriteAccess();
        User user = currentUserProvider.getCurrentUser();
        validateFormaPagamento(dto.contaBancariaId(), dto.cartaoCreditoId());

        Category categoria = resolveCategoria(dto.categoriaId(), user.getId());
        BankAccount contaBancaria = resolveContaBancaria(dto.contaBancariaId(), user.getId());
        CreditCard cartaoCredito = resolveCartaoCredito(dto.cartaoCreditoId(), user.getId());

        Subscription subscription = Subscription.builder()
                .user(user)
                .categoria(categoria)
                .contaBancaria(contaBancaria)
                .cartaoCredito(cartaoCredito)
                .descricao(dto.descricao())
                .valor(dto.valor())
                .diaCobranca(dto.diaCobranca())
                .dataInicio(dto.dataInicio())
                .dataFim(dto.dataFim())
                .ativa(dto.ativa() == null || dto.ativa())
                .observacoes(dto.observacoes())
                .build();

        Subscription saved = subscriptionRepository.save(subscription);
        return subscriptionMapper.toResponseDto(saved);
    }

    @Override
    public SubscriptionResponseDTO update(Long id, SubscriptionRequestDTO dto) {
        currentUserProvider.requireWriteAccess();
        Subscription subscription = findOwnedEntityById(id);
        validateFormaPagamento(dto.contaBancariaId(), dto.cartaoCreditoId());

        Long userId = subscription.getUser().getId();
        Category categoria = resolveCategoria(dto.categoriaId(), userId);
        BankAccount contaBancaria = resolveContaBancaria(dto.contaBancariaId(), userId);
        CreditCard cartaoCredito = resolveCartaoCredito(dto.cartaoCreditoId(), userId);

        subscription.setCategoria(categoria);
        subscription.setContaBancaria(contaBancaria);
        subscription.setCartaoCredito(cartaoCredito);
        subscription.setDescricao(dto.descricao());
        subscription.setValor(dto.valor());
        subscription.setDiaCobranca(dto.diaCobranca());
        subscription.setDataInicio(dto.dataInicio());
        subscription.setDataFim(dto.dataFim());
        if (dto.ativa() != null) {
            subscription.setAtiva(dto.ativa());
        }
        subscription.setObservacoes(dto.observacoes());

        Subscription updated = subscriptionRepository.save(subscription);
        return subscriptionMapper.toResponseDto(updated);
    }

    @Override
    public void delete(Long id) {
        currentUserProvider.requireWriteAccess();
        // Unconditional: subscriptions are ongoing generators, not fixed plans. Already
        // generated despesas survive via ON DELETE SET NULL on assinatura_id.
        Subscription subscription = findOwnedEntityById(id);
        subscriptionRepository.delete(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponseDTO findById(Long id) {
        return subscriptionMapper.toResponseDto(findOwnedEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponseDTO> findAll() {
        List<Long> scopeUserIds = currentUserProvider.getScopeUserIds();
        return subscriptionRepository.findAllByUserIdIn(scopeUserIds).stream()
                .map(subscriptionMapper::toResponseDto)
                .toList();
    }

    @Override
    public List<ExpenseResponseDTO> gerarPendentes() {
        List<Long> scopeUserIds = currentUserProvider.getScopeUserIds();
        LocalDate hoje = LocalDate.now();

        List<ExpenseResponseDTO> criadas = new ArrayList<>();
        for (Subscription assinatura : subscriptionRepository.findAllByUserIdInAndAtivaTrue(scopeUserIds)) {
            criadas.addAll(gerarCobrancasParaAssinatura(assinatura, hoje));
        }
        return criadas;
    }

    /**
     * Entity-only, no {@link CurrentUserProvider} call inside — safe to invoke from the
     * background {@code AssinaturaSchedulerJob}, which has no authenticated security context.
     * Public (rather than package-private) so the scheduler, in a different package, can call it.
     */
    public List<ExpenseResponseDTO> gerarCobrancasParaAssinatura(Subscription assinatura, LocalDate hoje) {
        List<ExpenseResponseDTO> criadas = new ArrayList<>();
        List<LocalDate> datasDevidas = subscriptionChargeScheduler.calcularDatasDevidas(
                assinatura.getDataInicio(), assinatura.getDataFim(), assinatura.getDiaCobranca(), hoje);

        for (LocalDate data : datasDevidas) {
            LocalDate referenciaMes = data.withDayOfMonth(1);
            if (expenseRepository.existsByAssinaturaIdAndReferenciaMes(assinatura.getId(), referenciaMes)) {
                continue;
            }
            try {
                Expense expense = Expense.builder()
                        .user(assinatura.getUser())
                        .categoria(assinatura.getCategoria())
                        .contaBancaria(assinatura.getContaBancaria())
                        .cartaoCredito(assinatura.getCartaoCredito())
                        .descricao(assinatura.getDescricao())
                        .valor(assinatura.getValor())
                        .data(data)
                        .referenciaMes(referenciaMes)
                        .assinatura(assinatura)
                        .paga(false)
                        .build();
                Expense saved = expenseRepository.save(expense);
                criadas.add(expenseMapper.toResponseDto(saved));
            } catch (DataIntegrityViolationException e) {
                log.warn("Cobrança já existente para a assinatura {} no mês de referência {} — ignorando",
                        assinatura.getId(), referenciaMes);
            }
        }
        return criadas;
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
            throw new BusinessException("A categoria informada não é válida para assinaturas");
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

    private Subscription findOwnedEntityById(Long id) {
        List<Long> scopeUserIds = currentUserProvider.getScopeUserIds();
        return subscriptionRepository.findByIdAndUserIdIn(id, scopeUserIds)
                .orElseThrow(() -> ResourceNotFoundException.of("Assinatura", id));
    }

}
