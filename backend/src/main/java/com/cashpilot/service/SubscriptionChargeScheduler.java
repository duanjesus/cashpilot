package com.cashpilot.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure math for computing which monthly subscription-charge dates are due within a window.
 * No persistence dependencies on purpose — trivially unit-testable without mocks, mirrors
 * {@link ProjectionCalculator}.
 */
@Component
public class SubscriptionChargeScheduler {

    private static final int MAX_MESES = 1200;

    public List<LocalDate> calcularDatasDevidas(LocalDate dataInicio, LocalDate dataFim, int diaCobranca, LocalDate ateData) {
        List<LocalDate> datas = new ArrayList<>();
        LocalDate mes = dataInicio.withDayOfMonth(1);
        for (int i = 0; i < MAX_MESES; i++) {
            if (dataFim != null && mes.isAfter(dataFim.withDayOfMonth(1))) break;
            LocalDate dataCobranca = mes.withDayOfMonth(Math.min(diaCobranca, mes.lengthOfMonth()));
            if (dataCobranca.isAfter(ateData)) break;
            if (!dataCobranca.isBefore(dataInicio)) datas.add(dataCobranca);
            mes = mes.plusMonths(1);
        }
        return datas;
    }

}
