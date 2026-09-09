package com.pack.scheduler;

import com.pack.service.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Runs on the 1st of every month at 00:05 server time and rolls forward any
 * budget flagged autoRenew=true from the previous month into the current one.
 * Safe to re-run: rollover skips periods that already have a budget.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BudgetRolloverScheduler {

    private final BudgetService budgetService;

    @Scheduled(cron = "0 5 0 1 * *")
    public void rolloverPreviousMonth() {
        LocalDate previousMonth = LocalDate.now().minusMonths(1);
        log.info("Scheduled budget rollover triggered for {}/{}",
                previousMonth.getMonthValue(), previousMonth.getYear());

        try {
            int created = budgetService.rolloverAutoRenewBudgets(
                    previousMonth.getMonthValue(), previousMonth.getYear());
            log.info("Scheduled budget rollover finished — {} budget(s) created", created);
        } catch (Exception ex) {
            log.error("Scheduled budget rollover failed", ex);
        }
    }
}
