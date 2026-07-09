package com.cashpilot.scheduler;

import com.cashpilot.entity.CreditCard;
import com.cashpilot.entity.Expense;
import com.cashpilot.entity.FinancialGoal;
import com.cashpilot.entity.Income;
import com.cashpilot.entity.Notification;
import com.cashpilot.entity.User;
import com.cashpilot.entity.enums.GoalType;
import com.cashpilot.entity.enums.NotificationTipo;
import com.cashpilot.repository.CreditCardRepository;
import com.cashpilot.repository.ExpenseRepository;
import com.cashpilot.repository.FinancialGoalRepository;
import com.cashpilot.repository.IncomeRepository;
import com.cashpilot.repository.NotificationRepository;
import com.cashpilot.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Daily cron job that generates in-app notifications system-wide: bills due soon, credit
 * card statements closing soon, and financial goals that have been reached. Runs with no
 * authenticated security context, so it must never call {@code CurrentUserProvider}'s
 * principal-resolving methods ({@code getCurrentUser}/{@code getScopeUserIds}/etc.) — only
 * {@link CurrentUserProvider#resolveScopeUserIds(Long)}, which takes an explicit owner id
 * instead of reading {@code SecurityContextHolder}.
 */
@Component
@RequiredArgsConstructor
public class NotificationSchedulerJob {

    private static final Logger log = LoggerFactory.getLogger(NotificationSchedulerJob.class);
    private static final DateTimeFormatter ANO_MES_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final CreditCardRepository creditCardRepository;
    private final FinancialGoalRepository financialGoalRepository;
    private final NotificationRepository notificationRepository;
    private final CurrentUserProvider currentUserProvider;

    @Scheduled(cron = "0 0 3 * * *")
    public void gerarNotificacoesDiarias() {
        List<Notification> geradas = gerarNotificacoesPendentes();
        log.info("Geradas {} notificação(ões) pendente(s)", geradas.size());
    }

    /**
     * Entity-only, no principal resolution inside beyond {@code resolveScopeUserIds(ownerId)} —
     * safe to invoke from this scheduler and from {@code NotificationServiceImpl.gerarPendentes()}
     * (the manual "generate now" endpoint), exactly like {@code SubscriptionServiceImpl}'s shared
     * generation method. Public so both callers, in different packages, can use it.
     */
    public List<Notification> gerarNotificacoesPendentes() {
        LocalDate hoje = LocalDate.now();
        List<Notification> geradas = new ArrayList<>();
        geradas.addAll(gerarContasAVencer(hoje));
        geradas.addAll(gerarFaturasFechando(hoje));
        geradas.addAll(gerarMetasAtingidas());
        return geradas;
    }

    private List<Notification> gerarContasAVencer(LocalDate hoje) {
        List<Notification> geradas = new ArrayList<>();
        LocalDate limite = hoje.plusDays(3);

        for (Expense expense : expenseRepository.findAll()) {
            if (Boolean.TRUE.equals(expense.getPaga())) {
                continue;
            }
            LocalDate data = expense.getData();
            if (data.isBefore(hoje) || data.isAfter(limite)) {
                continue;
            }
            String mensagem = "Despesa '" + expense.getDescricao() + "' de R$ " + expense.getValor()
                    + " vence em " + expense.getData();
            geradas.addAll(notifyScope(expense.getUser(), NotificationTipo.CONTA_A_VENCER, mensagem,
                    "DESPESA", expense.getId(), null));
        }

        for (Income income : incomeRepository.findAll()) {
            if (Boolean.TRUE.equals(income.getRecebida())) {
                continue;
            }
            LocalDate data = income.getData();
            if (data.isBefore(hoje) || data.isAfter(limite)) {
                continue;
            }
            String mensagem = "Receita '" + income.getDescricao() + "' de R$ " + income.getValor()
                    + " vence em " + income.getData();
            geradas.addAll(notifyScope(income.getUser(), NotificationTipo.CONTA_A_VENCER, mensagem,
                    "RECEITA", income.getId(), null));
        }

        return geradas;
    }

    private List<Notification> gerarFaturasFechando(LocalDate hoje) {
        List<Notification> geradas = new ArrayList<>();

        for (CreditCard cartao : creditCardRepository.findAll()) {
            if (!Boolean.TRUE.equals(cartao.getAtivo())) {
                continue;
            }
            LocalDate fechamento = proximoFechamento(cartao.getDiaFechamento(), hoje);
            if (fechamento.isAfter(hoje.plusDays(3))) {
                continue;
            }
            String anoMes = fechamento.format(ANO_MES_FORMATTER);
            String mensagem = "Fatura do cartão '" + cartao.getNome() + "' fecha em " + fechamento;
            geradas.addAll(notifyScope(cartao.getUser(), NotificationTipo.FATURA_FECHANDO, mensagem,
                    "CARTAO", cartao.getId(), anoMes));
        }

        return geradas;
    }

    private LocalDate proximoFechamento(Integer diaFechamento, LocalDate hoje) {
        YearMonth mesAtual = YearMonth.from(hoje);
        int diaClampeadoMesAtual = Math.min(diaFechamento, mesAtual.lengthOfMonth());
        if (hoje.getDayOfMonth() <= diaFechamento) {
            return mesAtual.atDay(diaClampeadoMesAtual);
        }
        YearMonth proximoMes = mesAtual.plusMonths(1);
        int diaClampeadoProximoMes = Math.min(diaFechamento, proximoMes.lengthOfMonth());
        return proximoMes.atDay(diaClampeadoProximoMes);
    }

    private List<Notification> gerarMetasAtingidas() {
        List<Notification> geradas = new ArrayList<>();

        for (FinancialGoal goal : financialGoalRepository.findAll()) {
            if (!Boolean.TRUE.equals(goal.getAtiva())) {
                continue;
            }
            BigDecimal valorAtual = resolverValorAtual(goal);
            if (valorAtual.compareTo(goal.getValorAlvo()) < 0) {
                continue;
            }
            String mensagem = "Meta '" + goal.getNome() + "' foi atingida!";
            geradas.addAll(notifyScope(goal.getUser(), NotificationTipo.META_ATINGIDA, mensagem,
                    "META", goal.getId(), null));
        }

        return geradas;
    }

    private BigDecimal resolverValorAtual(FinancialGoal goal) {
        if (goal.getTipo() == GoalType.INVESTIMENTO) {
            List<Long> scopeUserIds = currentUserProvider.resolveScopeUserIds(goal.getUser().getId());
            return expenseRepository.sumValorByUserIdAndDataBetweenAndCategoriaIsInvestment(
                    scopeUserIds, goal.getDataInicio(), LocalDate.now());
        }
        return goal.getValorAtual();
    }

    private List<Notification> notifyScope(User owner, NotificationTipo tipo, String mensagem,
                                            String referenciaTipo, Long referenciaId, String anoMesReferencia) {
        List<Notification> geradas = new ArrayList<>();
        List<Long> interessados = currentUserProvider.resolveScopeUserIds(owner.getId());

        for (Long userId : interessados) {
            boolean jaExiste = notificationRepository.existsByUserIdAndTipoAndReferenciaTipoAndReferenciaIdAndAnoMesReferencia(
                    userId, tipo, referenciaTipo, referenciaId, anoMesReferencia);
            if (jaExiste) {
                continue;
            }
            User destinatario = userId.equals(owner.getId()) ? owner : User.builder().id(userId).build();
            Notification notification = Notification.builder()
                    .user(destinatario)
                    .tipo(tipo)
                    .mensagem(mensagem)
                    .referenciaTipo(referenciaTipo)
                    .referenciaId(referenciaId)
                    .anoMesReferencia(anoMesReferencia)
                    .lida(false)
                    .build();
            geradas.add(notificationRepository.save(notification));
        }

        return geradas;
    }

}
