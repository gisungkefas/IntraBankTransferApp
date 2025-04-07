package com.example.MoneyTransferApp.repository;

import com.example.MoneyTransferApp.entity.TransactionSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface TransactionSummaryRepository extends JpaRepository<TransactionSummary, Long> {

    Optional<TransactionSummary> findByDate(LocalDate date);
}
