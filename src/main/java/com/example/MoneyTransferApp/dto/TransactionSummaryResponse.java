package com.example.MoneyTransferApp.dto;

import com.example.MoneyTransferApp.entity.TransactionSummary;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class TransactionSummaryResponse {

    private LocalDate date;
    private long totalTransactions;
    private long successfulTransactions;
    private long failedTransactions;
    private BigDecimal totalAmount;
    private BigDecimal totalFees;
    private BigDecimal totalCommission;

    public static TransactionSummaryResponse fromTransactionSummary(TransactionSummary summary) {
        return TransactionSummaryResponse.builder()
                .date(summary.getDate())
                .totalTransactions(summary.getTotalTransactions())
                .successfulTransactions(summary.getSuccessfulTransactions())
                .failedTransactions(summary.getFailedTransactions())
                .totalAmount(summary.getTotalAmount())
                .totalFees(summary.getTotalFees())
                .totalCommission(summary.getTotalCommission())
                .build();
    }
}
