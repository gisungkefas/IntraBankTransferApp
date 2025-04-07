package com.example.MoneyTransferApp.dto;

import com.example.MoneyTransferApp.entity.Transaction;
import com.example.MoneyTransferApp.enums.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {

    private String id;
    private String reference;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private BigDecimal transactionFee;
    private BigDecimal billedAmount;
    private String description;
    private TransactionStatus status;
    private String statusMessage;
    private boolean commissionWorthy;
    private BigDecimal commission;
    private LocalDateTime createdAt;

    public static TransactionResponse fromTransaction(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .reference(transaction.getReference())
                .sourceAccountNumber(transaction.getSourceAccountNumber())
                .destinationAccountNumber(transaction.getDestinationAccountNumber())
                .amount(transaction.getAmount())
                .transactionFee(transaction.getTransactionFee())
                .billedAmount(transaction.getBilledAmount())
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .statusMessage(transaction.getStatusMessage())
                .commissionWorthy(transaction.isCommissionWorthy())
                .commission(transaction.getCommission())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
