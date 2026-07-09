package com.cashpilot.scheduler;

import com.cashpilot.entity.Subscription;
import com.cashpilot.repository.SubscriptionRepository;
import com.cashpilot.service.impl.SubscriptionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Daily cron job that generates pending subscription charges system-wide. Runs with no
 * authenticated security context, so it must never touch {@code CurrentUserProvider} —
 * it goes straight to {@link SubscriptionRepository#findAllByAtivaTrue()} (no user scoping)
 * and the entity-only {@code gerarCobrancasParaAssinatura} helper.
 */
@Component
@RequiredArgsConstructor
public class AssinaturaSchedulerJob {

    private static final Logger log = LoggerFactory.getLogger(AssinaturaSchedulerJob.class);

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionServiceImpl subscriptionServiceImpl;

    @Scheduled(cron = "0 0 2 * * *")
    public void gerarCobrancasDiarias() {
        LocalDate hoje = LocalDate.now();
        List<Subscription> ativas = subscriptionRepository.findAllByAtivaTrue();
        log.info("Gerando cobranças pendentes para {} assinatura(s) ativa(s)", ativas.size());
        for (Subscription assinatura : ativas) {
            subscriptionServiceImpl.gerarCobrancasParaAssinatura(assinatura, hoje);
        }
    }

}
