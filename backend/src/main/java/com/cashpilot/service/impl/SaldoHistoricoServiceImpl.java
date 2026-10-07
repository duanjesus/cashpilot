package com.cashpilot.service.impl;

import com.cashpilot.dto.response.CapturaSaldoResponseDTO;
import com.cashpilot.dto.response.SaldoHistoricoPontoDTO;
import com.cashpilot.entity.BankAccount;
import com.cashpilot.entity.SaldoDiario;
import com.cashpilot.entity.enums.SaldoOrigem;
import com.cashpilot.exception.BusinessException;
import com.cashpilot.exception.ResourceNotFoundException;
import com.cashpilot.repository.BankAccountRepository;
import com.cashpilot.repository.SaldoDiarioRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.SaldoCalculator;
import com.cashpilot.service.SaldoHistoricoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SaldoHistoricoServiceImpl implements SaldoHistoricoService {

    /** Longest window a caller may query, and how far back missing snapshots are back-filled. */
    public static final int MAX_DIAS_HISTORICO = 730;

    private final BankAccountRepository bankAccountRepository;
    private final SaldoDiarioRepository saldoDiarioRepository;
    private final SaldoCalculator saldoCalculator;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public List<SaldoHistoricoPontoDTO> getHistoricoConta(Long contaId, int dias) {
        validarPeriodo(dias);
        BankAccount conta = bankAccountRepository.findByIdAndUserIdIn(contaId, currentUserProvider.getScopeUserIds())
                .orElseThrow(() -> ResourceNotFoundException.of("Conta bancária", contaId));

        LocalDate hoje = LocalDate.now();
        return List.copyOf(historicoDaConta(conta, hoje.minusDays(dias - 1L), hoje).values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaldoHistoricoPontoDTO> getHistoricoTotal(int dias) {
        validarPeriodo(dias);
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.minusDays(dias - 1L);

        SortedMap<LocalDate, SaldoHistoricoPontoDTO> total = new TreeMap<>();
        for (BankAccount conta : bankAccountRepository.findAllByUserIdIn(currentUserProvider.getScopeUserIds())) {
            if (!Boolean.TRUE.equals(conta.getAtiva())) {
                continue;
            }
            historicoDaConta(conta, inicio, hoje).forEach((dia, ponto) -> total.merge(dia, ponto, this::somar));
        }
        return List.copyOf(total.values());
    }

    @Override
    public CapturaSaldoResponseDTO capturarPendentes() {
        currentUserProvider.requireWriteAccess();
        LocalDate hoje = LocalDate.now();

        int criados = 0;
        for (BankAccount conta : bankAccountRepository.findAllByUserIdIn(currentUserProvider.getScopeUserIds())) {
            criados += capturarParaConta(conta, hoje);
        }
        return new CapturaSaldoResponseDTO(criados);
    }

    /**
     * Writes one snapshot per day still missing up to yesterday, starting after the account's
     * last snapshot (or at its opening date when it has none). Entity-only, no
     * {@link CurrentUserProvider} call inside — safe to invoke from {@code SaldoSnapshotJob},
     * which has no authenticated security context. Idempotent: a second run finds nothing missing.
     *
     * @return how many snapshots were written
     */
    public int capturarParaConta(BankAccount conta, LocalDate hoje) {
        LocalDate ontem = hoje.minusDays(1);
        LocalDate inicio = saldoDiarioRepository.findUltimaData(conta.getId())
                .map(ultima -> ultima.plusDays(1))
                .orElse(conta.getDataSaldoInicial());
        LocalDate limite = hoje.minusDays(MAX_DIAS_HISTORICO);
        if (inicio.isBefore(limite)) {
            inicio = limite;
        }
        if (inicio.isAfter(ontem)) {
            return 0;
        }

        List<SaldoDiario> novos = new ArrayList<>();
        saldoCalculator.serieDiaria(conta, inicio, ontem).forEach((dia, saldo) -> novos.add(SaldoDiario.builder()
                .contaBancaria(conta)
                .data(dia)
                .saldo(saldo)
                .origem(dia.equals(ontem) ? SaldoOrigem.CAPTURADO : SaldoOrigem.RECONSTRUIDO)
                .build()));
        saldoDiarioRepository.saveAll(novos);
        return novos.size();
    }

    /** Balance history of one account, never reaching back before the account's opening date. */
    private SortedMap<LocalDate, SaldoHistoricoPontoDTO> historicoDaConta(BankAccount conta, LocalDate inicio, LocalDate fim) {
        SortedMap<LocalDate, SaldoHistoricoPontoDTO> pontos = new TreeMap<>();
        LocalDate inicioEfetivo = inicio.isBefore(conta.getDataSaldoInicial()) ? conta.getDataSaldoInicial() : inicio;
        if (inicioEfetivo.isAfter(fim)) {
            return pontos;
        }

        Map<LocalDate, SaldoDiario> registros = saldoDiarioRepository
                .findAllByContaBancariaIdAndDataBetween(conta.getId(), inicioEfetivo, fim).stream()
                .collect(Collectors.toMap(SaldoDiario::getData, Function.identity()));

        saldoCalculator.serieDiaria(conta, inicioEfetivo, fim).forEach((dia, saldo) -> {
            SaldoDiario registro = registros.get(dia);
            pontos.put(dia, registro == null
                    ? ponto(dia, saldo, null, null)
                    : ponto(dia, saldo, registro.getSaldo(), registro.getOrigem()));
        });
        return pontos;
    }

    /** Combines two accounts' points for the same day; an account without a snapshot contributes its recomputed balance. */
    private SaldoHistoricoPontoDTO somar(SaldoHistoricoPontoDTO a, SaldoHistoricoPontoDTO b) {
        BigDecimal saldo = a.saldo().add(b.saldo());
        if (a.saldoRegistrado() == null && b.saldoRegistrado() == null) {
            return ponto(a.data(), saldo, null, null);
        }
        BigDecimal registrado = registradoOuSaldo(a).add(registradoOuSaldo(b));
        SaldoOrigem origem = a.origem() == SaldoOrigem.CAPTURADO && b.origem() == SaldoOrigem.CAPTURADO
                ? SaldoOrigem.CAPTURADO
                : SaldoOrigem.RECONSTRUIDO;
        return ponto(a.data(), saldo, registrado, origem);
    }

    private BigDecimal registradoOuSaldo(SaldoHistoricoPontoDTO ponto) {
        return ponto.saldoRegistrado() != null ? ponto.saldoRegistrado() : ponto.saldo();
    }

    private SaldoHistoricoPontoDTO ponto(LocalDate dia, BigDecimal saldo, BigDecimal registrado, SaldoOrigem origem) {
        boolean divergente = registrado != null && registrado.compareTo(saldo) != 0;
        return new SaldoHistoricoPontoDTO(dia, saldo, registrado, origem, divergente);
    }

    private void validarPeriodo(int dias) {
        if (dias < 1 || dias > MAX_DIAS_HISTORICO) {
            throw new BusinessException("O período deve estar entre 1 e " + MAX_DIAS_HISTORICO + " dias");
        }
    }

}
