package com.example.MoneyTransferApp.service;

import com.example.MoneyTransferApp.dto.TransactionFilterRequest;
import com.example.MoneyTransferApp.dto.TransactionResponse;
import com.example.MoneyTransferApp.dto.TransactionSummaryResponse;
import com.example.MoneyTransferApp.dto.TransferRequest;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {

    TransactionResponse processTransfer(TransferRequest request);

    Page<TransactionResponse> getTransactionsWithFilters(TransactionFilterRequest filterRequest);

    void updateCommissionWorthy();

    TransactionSummaryResponse getTransactionSummary(LocalDate date);

    TransactionSummaryResponse generateTransactionSummary(LocalDate date);
}
