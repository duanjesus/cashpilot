package com.cashpilot.service.impl;

import com.cashpilot.dto.response.RelatorioMensalDTO;
import com.cashpilot.repository.ExpenseRepository;
import com.cashpilot.repository.IncomeRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelatorioServiceImpl implements RelatorioService {

    private static final DateTimeFormatter ANO_MES_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public List<RelatorioMensalDTO> getRelatorioMensal(int meses) {
        List<Long> scopeUserIds = currentUserProvider.getScopeUserIds();

        List<RelatorioMensalDTO> relatorios = new ArrayList<>();
        for (int i = meses - 1; i >= 0; i--) {
            LocalDate mes = LocalDate.now().minusMonths(i);
            LocalDate primeiroDia = mes.withDayOfMonth(1);
            LocalDate ultimoDia = mes.withDayOfMonth(mes.lengthOfMonth());

            BigDecimal entradas = incomeRepository.sumValorByUserIdAndDataBetween(scopeUserIds, primeiroDia, ultimoDia);
            BigDecimal saidas = expenseRepository.sumValorByUserIdAndDataBetween(scopeUserIds, primeiroDia, ultimoDia);
            BigDecimal investimentos = expenseRepository.sumValorByUserIdAndDataBetweenAndCategoriaIsInvestment(scopeUserIds, primeiroDia, ultimoDia);
            BigDecimal saldoLiquido = entradas.subtract(saidas);

            relatorios.add(new RelatorioMensalDTO(mes.format(ANO_MES_FORMATTER), entradas, saidas, investimentos, saldoLiquido));
        }
        return relatorios;
    }

}
