package com.example.MoneyTransferApp.scheduler;

import com.example.MoneyTransferApp.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionScheduler {

    private final TransactionService transactionService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void updateCommissionWorthy() {
        log.info("Running scheduled job to update commission worthy transactions");
        transactionService.updateCommissionWorthy();
    }

    @Scheduled(cron = "0 30 2 * * ?")
    public void generateDailySummary() {
        log.info("Running scheduled job to generate daily transaction summary");
        LocalDate yesterday = LocalDate.now().minusDays(1);
        transactionService.generateTransactionSummary(yesterday);
    }
}
