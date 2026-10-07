package com.cashpilot.scheduler;

import com.cashpilot.entity.BankAccount;
import com.cashpilot.repository.BankAccountRepository;
import com.cashpilot.service.impl.SaldoHistoricoServiceImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Daily cron job that records yesterday's closing balance for every bank account system-wide,
 * back-filling any days missed while the application was down. Runs after the subscription
 * (02:00) and notification (03:00) jobs. Has no authenticated security context, so it must
 * never touch {@code CurrentUserProvider} — it goes straight to the repository and the
 * entity-only {@code capturarParaConta} helper.
 */
@Component
@RequiredArgsConstructor
public class SaldoSnapshotJob {

    private static final Logger log = LoggerFactory.getLogger(SaldoSnapshotJob.class);

    private final BankAccountRepository bankAccountRepository;
    private final SaldoHistoricoServiceImpl saldoHistoricoServiceImpl;

    @Scheduled(cron = "0 0 4 * * *")
    public void capturarSaldosDiarios() {
        LocalDate hoje = LocalDate.now();
        int criados = 0;
        for (BankAccount conta : bankAccountRepository.findAll()) {
            criados += saldoHistoricoServiceImpl.capturarParaConta(conta, hoje);
        }
        log.info("Registrado(s) {} saldo(s) diário(s)", criados);
    }

}
