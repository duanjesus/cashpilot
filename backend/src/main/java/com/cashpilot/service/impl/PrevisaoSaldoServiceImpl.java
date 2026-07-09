package com.cashpilot.service.impl;

import com.cashpilot.dto.response.PrevisaoSaldoPontoDTO;
import com.cashpilot.dto.response.PrevisaoSaldoResponseDTO;
import com.cashpilot.dto.response.RelatorioMensalDTO;
import com.cashpilot.service.BankAccountService;
import com.cashpilot.service.PrevisaoSaldoService;
import com.cashpilot.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrevisaoSaldoServiceImpl implements PrevisaoSaldoService {

    private static final DateTimeFormatter ANO_MES_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final int MESES_HISTORICO_PADRAO = 6;
    private static final int MESES_PROJECAO_PADRAO = 12;

    private final RelatorioService relatorioService;
    private final BankAccountService bankAccountService;

    @Override
    public PrevisaoSaldoResponseDTO getPrevisao(Integer mesesHistorico, Integer mesesProjecao) {
        int historico = mesesHistorico != null ? mesesHistorico : MESES_HISTORICO_PADRAO;
        int projecao = mesesProjecao != null ? mesesProjecao : MESES_PROJECAO_PADRAO;

        List<RelatorioMensalDTO> relatorios = relatorioService.getRelatorioMensal(historico);
        BigDecimal mediaMensalHistorica = calcularMedia(relatorios);

        BigDecimal saldoAtual = bankAccountService.getSaldoAtualTotal();

        List<PrevisaoSaldoPontoDTO> serie = new ArrayList<>();
        for (int m = 1; m <= projecao; m++) {
            LocalDate mesProjetado = LocalDate.now().plusMonths(m);
            BigDecimal saldoProjetado = saldoAtual.add(mediaMensalHistorica.multiply(BigDecimal.valueOf(m)));
            serie.add(new PrevisaoSaldoPontoDTO(mesProjetado.format(ANO_MES_FORMATTER), saldoProjetado));
        }

        return new PrevisaoSaldoResponseDTO(saldoAtual, mediaMensalHistorica, serie);
    }

    private BigDecimal calcularMedia(List<RelatorioMensalDTO> relatorios) {
        if (relatorios.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal soma = relatorios.stream()
                .map(RelatorioMensalDTO::saldoLiquido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return soma.divide(BigDecimal.valueOf(relatorios.size()), 2, RoundingMode.HALF_UP);
    }

}
