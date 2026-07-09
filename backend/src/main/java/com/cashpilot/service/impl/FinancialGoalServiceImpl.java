package com.cashpilot.service.impl;

import com.cashpilot.dto.request.FinancialGoalRequestDTO;
import com.cashpilot.dto.request.UpdateGoalProgressRequestDTO;
import com.cashpilot.dto.response.FinancialGoalResponseDTO;
import com.cashpilot.entity.FinancialGoal;
import com.cashpilot.entity.User;
import com.cashpilot.entity.enums.GoalType;
import com.cashpilot.exception.BusinessException;
import com.cashpilot.exception.ResourceNotFoundException;
import com.cashpilot.mapper.FinancialGoalMapper;
import com.cashpilot.repository.ExpenseRepository;
import com.cashpilot.repository.FinancialGoalRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.FinancialGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FinancialGoalServiceImpl implements FinancialGoalService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final FinancialGoalRepository financialGoalRepository;
    private final ExpenseRepository expenseRepository;
    private final FinancialGoalMapper financialGoalMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public FinancialGoalResponseDTO create(FinancialGoalRequestDTO dto) {
        currentUserProvider.requireWriteAccess();
        User user = currentUserProvider.getCurrentUser();

        FinancialGoal goal = FinancialGoal.builder()
                .user(user)
                .nome(dto.nome())
                .valorAlvo(dto.valorAlvo())
                .dataAlvo(dto.dataAlvo())
                .valorAtual(dto.valorAtual() == null ? BigDecimal.ZERO : dto.valorAtual())
                .ativa(dto.ativa() == null || dto.ativa())
                .tipo(dto.tipo() == null ? GoalType.MANUAL : dto.tipo())
                .dataInicio(dto.dataInicio() == null ? LocalDate.now() : dto.dataInicio())
                .build();

        FinancialGoal saved = financialGoalRepository.save(goal);
        return toResponseWithProgresso(saved);
    }

    @Override
    public FinancialGoalResponseDTO update(Long id, FinancialGoalRequestDTO dto) {
        currentUserProvider.requireWriteAccess();
        FinancialGoal goal = findOwnedEntityById(id);

        goal.setNome(dto.nome());
        goal.setValorAlvo(dto.valorAlvo());
        goal.setDataAlvo(dto.dataAlvo());
        if (dto.valorAtual() != null) {
            goal.setValorAtual(dto.valorAtual());
        }
        if (dto.ativa() != null) {
            goal.setAtiva(dto.ativa());
        }
        if (dto.tipo() != null) {
            goal.setTipo(dto.tipo());
        }
        if (dto.dataInicio() != null) {
            goal.setDataInicio(dto.dataInicio());
        }

        FinancialGoal updated = financialGoalRepository.save(goal);
        return toResponseWithProgresso(updated);
    }

    @Override
    public void delete(Long id) {
        currentUserProvider.requireWriteAccess();
        FinancialGoal goal = findOwnedEntityById(id);
        financialGoalRepository.delete(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialGoalResponseDTO findById(Long id) {
        return toResponseWithProgresso(findOwnedEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinancialGoalResponseDTO> findAll() {
        List<Long> scopeUserIds = currentUserProvider.getScopeUserIds();
        return financialGoalRepository.findAllByUserIdIn(scopeUserIds).stream()
                .map(this::toResponseWithProgresso)
                .toList();
    }

    @Override
    public FinancialGoalResponseDTO updateProgress(Long id, UpdateGoalProgressRequestDTO dto) {
        currentUserProvider.requireWriteAccess();
        FinancialGoal goal = findOwnedEntityById(id);
        if (goal.getTipo() == GoalType.INVESTIMENTO) {
            throw new BusinessException("Metas de investimento têm progresso calculado automaticamente a partir das despesas de investimento");
        }
        goal.setValorAtual(dto.valorAtual());

        FinancialGoal updated = financialGoalRepository.save(goal);
        return toResponseWithProgresso(updated);
    }

    private FinancialGoalResponseDTO toResponseWithProgresso(FinancialGoal goal) {
        FinancialGoalResponseDTO base = financialGoalMapper.toResponseDto(goal);
        BigDecimal valorAtual = goal.getTipo() == GoalType.INVESTIMENTO
                ? expenseRepository.sumValorByUserIdAndDataBetweenAndCategoriaIsInvestment(
                        currentUserProvider.getScopeUserIds(), goal.getDataInicio(), LocalDate.now())
                : goal.getValorAtual();
        BigDecimal progresso = calcularProgresso(valorAtual, goal.getValorAlvo());
        return new FinancialGoalResponseDTO(
                base.id(),
                base.nome(),
                base.valorAlvo(),
                base.dataAlvo(),
                valorAtual,
                base.ativa(),
                progresso,
                base.tipo(),
                base.dataInicio()
        );
    }

    private BigDecimal calcularProgresso(BigDecimal valorAtual, BigDecimal valorAlvo) {
        if (valorAlvo == null || valorAlvo.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal progresso = valorAtual.divide(valorAlvo, 4, RoundingMode.HALF_UP).multiply(HUNDRED);
        return progresso.compareTo(HUNDRED) > 0 ? HUNDRED : progresso;
    }

    private FinancialGoal findOwnedEntityById(Long id) {
        List<Long> scopeUserIds = currentUserProvider.getScopeUserIds();
        return financialGoalRepository.findByIdAndUserIdIn(id, scopeUserIds)
                .orElseThrow(() -> ResourceNotFoundException.of("Meta financeira", id));
    }

}
