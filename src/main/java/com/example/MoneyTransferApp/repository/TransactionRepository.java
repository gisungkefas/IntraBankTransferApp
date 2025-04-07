package com.example.MoneyTransferApp.repository;

import com.example.MoneyTransferApp.entity.Transaction;
import com.example.MoneyTransferApp.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findBySourceAccountNumberOrDestinationAccountNumber(
            String sourceAccountNumber, String destinationAccountNumber);

    List<Transaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:accountNumber IS NULL OR t.sourceAccountNumber = :accountNumber OR t.destinationAccountNumber = :accountNumber) AND " +
            "(:startDate IS NULL OR t.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR t.createdAt <= :endDate)")
    Page<Transaction> findTransactionsWithFilters(
            @Param("status") TransactionStatus status,
            @Param("accountNumber") String accountNumber,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    List<Transaction> findByStatusAndCommissionWorthy(TransactionStatus status, boolean commissionWorthy);

    @Query("SELECT t FROM Transaction t WHERE " +
            "t.status = 'SUCCESSFUL' AND t.commissionWorthy = false")
    List<Transaction> findSuccessfulTransactionsWithoutCommission();

    @Query("SELECT t FROM Transaction t WHERE DATE(t.createdAt) = :date")
    List<Transaction> findTransactionsByDate(LocalDate date);
}
