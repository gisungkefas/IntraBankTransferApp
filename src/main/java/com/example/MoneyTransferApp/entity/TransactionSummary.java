package com.example.MoneyTransferApp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transaction_summaries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "total_transactions")
    private long totalTransactions;

    @Column(name = "successful_transactions")
    private long successfulTransactions;

    @Column(name = "failed_transactions")
    private long failedTransactions;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "total_fees")
    private BigDecimal totalFees;

    @Column(name = "total_commission")
    private BigDecimal totalCommission;
}
