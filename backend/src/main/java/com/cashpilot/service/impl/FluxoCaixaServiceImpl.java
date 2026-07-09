package com.cashpilot.service.impl;

import com.cashpilot.dto.response.FluxoCaixaItemDTO;
import com.cashpilot.dto.response.FluxoCaixaPontoDTO;
import com.cashpilot.dto.response.FluxoCaixaResponseDTO;
import com.cashpilot.entity.Expense;
import com.cashpilot.entity.Income;
import com.cashpilot.entity.Subscription;
import com.cashpilot.repository.ExpenseRepository;
import com.cashpilot.repository.IncomeRepository;
import com.cashpilot.repository.SubscriptionRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.BankAccountService;
import com.cashpilot.service.FluxoCaixaService;
import com.cashpilot.service.SubscriptionChargeScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FluxoCaixaServiceImpl implements FluxoCaixaService {

    private final BankAccountService bankAccountService;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionChargeScheduler subscriptionChargeScheduler;
    private final CurrentUserProvider currentUserProvider;

    @Value("${cashpilot.fluxo-caixa.dias-padrao:30}")
    private int diasPadrao = 30;

    @Override
    public FluxoCaixaResponseDTO getFluxoCaixa(Integer dias) {
        List<Long> scopeUserIds = currentUserProvider.getScopeUserIds();
        int janela = dias != null ? dias : diasPadrao;

        LocalDate hoje = LocalDate.now();
        LocalDate dataFim = hoje.plusDays(janela);

        BigDecimal saldoInicial = bankAccountService.getSaldoAtualTotal();

        List<FluxoCaixaItemDTO> detalhamento = new ArrayList<>();
        detalhamento.addAll(coletarDespesasPendentes(scopeUserIds, hoje, dataFim));
        detalhamento.addAll(coletarReceitasPendentes(scopeUserIds, hoje, dataFim));
        detalhamento.addAll(coletarAssinaturasProjetadas(scopeUserIds, hoje, dataFim));

        List<FluxoCaixaPontoDTO> serie = construirSerie(hoje, dataFim, saldoInicial, detalhamento);

        return new FluxoCaixaResponseDTO(saldoInicial, serie, detalhamento);
    }

    private List<FluxoCaixaItemDTO> coletarDespesasPendentes(List<Long> scopeUserIds, LocalDate hoje, LocalDate dataFim) {
        List<FluxoCaixaItemDTO> itens = new ArrayList<>();
        for (Expense despesa : expenseRepository.findUpcomingUnpaid(scopeUserIds, hoje, dataFim)) {
            String origemNome = despesa.getCategoria() != null ? despesa.getCategoria().getNome() : null;
            itens.add(new FluxoCaixaItemDTO(despesa.getData(), despesa.getDescricao(), despesa.getValor().negate(),
                    "DESPESA_PENDENTE", origemNome));
        }
        return itens;
    }

    private List<FluxoCaixaItemDTO> coletarReceitasPendentes(List<Long> scopeUserIds, LocalDate hoje, LocalDate dataFim) {
        List<FluxoCaixaItemDTO> itens = new ArrayList<>();
        for (Income receita : incomeRepository.findAllByUserIdAndRecebidaFalseAndDataBetween(scopeUserIds, hoje, dataFim)) {
            String origemNome = receita.getCategoria() != null ? receita.getCategoria().getNome() : null;
            itens.add(new FluxoCaixaItemDTO(receita.getData(), receita.getDescricao(), receita.getValor(),
                    "RECEITA_PENDENTE", origemNome));
        }
        return itens;
    }

    private List<FluxoCaixaItemDTO> coletarAssinaturasProjetadas(List<Long> scopeUserIds, LocalDate hoje, LocalDate dataFim) {
        List<FluxoCaixaItemDTO> itens = new ArrayList<>();
        for (Subscription assinatura : subscriptionRepository.findAllByUserIdInAndAtivaTrue(scopeUserIds)) {
            List<LocalDate> datas = subscriptionChargeScheduler.calcularDatasDevidas(
                    assinatura.getDataInicio(), assinatura.getDataFim(), assinatura.getDiaCobranca(), dataFim);
            for (LocalDate data : datas) {
                if (data.isBefore(hoje)) {
                    continue;
                }
                LocalDate referenciaMes = data.withDayOfMonth(1);
                // Skip dates already materialized as a real despesa — those are already
                // captured (if still unpaid) by coletarDespesasPendentes, avoiding double-counting.
                if (expenseRepository.existsByAssinaturaIdAndReferenciaMes(assinatura.getId(), referenciaMes)) {
                    continue;
                }
                itens.add(new FluxoCaixaItemDTO(data, assinatura.getDescricao(), assinatura.getValor().negate(),
                        "ASSINATURA_PROJETADA", assinatura.getDescricao()));
            }
        }
        return itens;
    }

    private List<FluxoCaixaPontoDTO> construirSerie(LocalDate hoje, LocalDate dataFim, BigDecimal saldoInicial,
                                                      List<FluxoCaixaItemDTO> detalhamento) {
        Map<LocalDate, BigDecimal> deltasPorDia = new TreeMap<>();
        for (LocalDate d = hoje; !d.isAfter(dataFim); d = d.plusDays(1)) {
            deltasPorDia.put(d, BigDecimal.ZERO);
        }
        for (FluxoCaixaItemDTO item : detalhamento) {
            deltasPorDia.merge(item.data(), item.valor(), BigDecimal::add);
        }

        List<FluxoCaixaPontoDTO> serie = new ArrayList<>();
        BigDecimal acumulado = saldoInicial;
        for (Map.Entry<LocalDate, BigDecimal> entry : deltasPorDia.entrySet()) {
            acumulado = acumulado.add(entry.getValue());
            serie.add(new FluxoCaixaPontoDTO(entry.getKey(), acumulado));
        }
        return serie;
    }

}
