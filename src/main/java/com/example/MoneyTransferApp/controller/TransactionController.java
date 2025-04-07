package com.example.MoneyTransferApp.controller;

import com.example.MoneyTransferApp.dto.*;
import com.example.MoneyTransferApp.enums.TransactionStatus;
import com.example.MoneyTransferApp.exception.AccountNotFoundException;
import com.example.MoneyTransferApp.exception.InsufficientFundsException;
import com.example.MoneyTransferApp.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<?> transferFunds(@Valid @RequestBody TransferRequest request) {
        log.info("Received transfer request from account {} to account {}",
                request.getSourceAccountNumber(), request.getDestinationAccountNumber());

        TransactionResponse response = transactionService.processTransfer(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<TransactionResponse>> getTransactions(
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching paginated transactions - status: {}, accountNumber: {}, startDate: {}, endDate: {}, page: {}, size: {}",
                status, accountNumber, startDate, endDate, page, size);

        TransactionFilterRequest filterRequest = new TransactionFilterRequest();
        filterRequest.setStatus(status);
        filterRequest.setAccountNumber(accountNumber);
        filterRequest.setStartDate(startDate != null ? startDate.atStartOfDay() : null);
        filterRequest.setEndDate(endDate != null ? endDate.atTime(LocalTime.MAX) : null);
        filterRequest.setPage(Math.max(page - 1, 0));
        filterRequest.setSize(size);

        Page<TransactionResponse> transactionPage = transactionService.getTransactionsWithFilters(filterRequest);

        PagedResponse<TransactionResponse> response = new PagedResponse<>();
        response.setContent(transactionPage.getContent());
        response.setTotalPages(transactionPage.getTotalPages());
        response.setTotalElements(transactionPage.getTotalElements());
        response.setLast(transactionPage.isLast());
        response.setSize(transactionPage.getSize());
        response.setNumber(transactionPage.getNumber());
        response.setFirst(transactionPage.isFirst());
        response.setNumberOfElements(transactionPage.getNumberOfElements());
        response.setEmpty(transactionPage.isEmpty());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<TransactionSummaryResponse> getTransactionSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Fetching transaction summary for date: {}", date);

        if (date.isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest().build();
        }

        TransactionSummaryResponse summary = transactionService.getTransactionSummary(date);
        return ResponseEntity.ok(summary);
    }
}
