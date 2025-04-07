package com.example.MoneyTransferApp.service.serviceImpl;

import com.example.MoneyTransferApp.dto.TransactionFilterRequest;
import com.example.MoneyTransferApp.dto.TransactionResponse;
import com.example.MoneyTransferApp.dto.TransactionSummaryResponse;
import com.example.MoneyTransferApp.dto.TransferRequest;
import com.example.MoneyTransferApp.entity.Account;
import com.example.MoneyTransferApp.entity.Transaction;
import com.example.MoneyTransferApp.entity.TransactionSummary;
import com.example.MoneyTransferApp.enums.TransactionStatus;
import com.example.MoneyTransferApp.exception.AccountNotFoundException;
import com.example.MoneyTransferApp.exception.InsufficientFundsException;
import com.example.MoneyTransferApp.exception.InvalidTransactionException;
import com.example.MoneyTransferApp.repository.AccountRepository;
import com.example.MoneyTransferApp.repository.TransactionRepository;
import com.example.MoneyTransferApp.repository.TransactionSummaryRepository;
import com.example.MoneyTransferApp.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    @Value("${transfer.fee.percentage}")
    private String feePercentageStr;

    @Value("${transfer.fee.cap}")
    private String feeCapStr;

    @Value("${transfer.commission.percentage}")
    private String commissionPercentageStr;

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionSummaryRepository transactionSummaryRepository;

    @Transactional
    public TransactionResponse processTransfer(TransferRequest request) {
        log.info("Processing transfer request: {}", request);

        if (request.getSourceAccountNumber().equals(request.getDestinationAccountNumber())) {
            throw new InvalidTransactionException("Source and destination accounts cannot be the same");
        }

        String reference = UUID.randomUUID().toString();

        Account source = accountRepository.findByAccountNumber(request.getSourceAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Source account not found"));

        Account dest = accountRepository.findByAccountNumber(request.getDestinationAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Destination account not found"));

        BigDecimal transactionFee = calculateTransactionFee(request.getAmount());
        BigDecimal billedAmount = request.getAmount().add(transactionFee);

        if (source.getBalance().compareTo(billedAmount) < 0) {
            log.warn("Insufficient funds in account {}. Required: {}, Available: {}",
                    source.getAccountNumber(), billedAmount, source.getBalance());
            throw new InsufficientFundsException("Insufficient funds");
        }

        Transaction transaction = buildInitialTransaction(request, reference, transactionFee, billedAmount);
        transaction.setStatus(TransactionStatus.PROCESSING);
        transaction.setStatusMessage("Transaction is being processed");

        transaction = transactionRepository.save(transaction);

        try {
            BigDecimal newSourceBalance = source.getBalance().subtract(billedAmount);
            source.setBalance(newSourceBalance);
            accountRepository.save(source);
            log.info("Debited source account {} with total {} (amount {} + fee {})",
                    source.getAccountNumber(), billedAmount, request.getAmount(), transactionFee);

            BigDecimal newDestBalance = dest.getBalance().add(request.getAmount());
            dest.setBalance(newDestBalance);
            accountRepository.save(dest);
            log.info("Credited destination account {} with amount {}", dest.getAccountNumber(), request.getAmount());

            transaction.setStatus(TransactionStatus.SUCCESSFUL);
            transaction.setStatusMessage("Transfer completed successfully");

        } catch (Exception ex) {
            log.error("Transfer failed during processing: {}", ex.getMessage(), ex);
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setStatusMessage("Processing error: " + ex.getMessage());
        }

        return saveAndReturnResponse(transaction);
    }

    public Page<TransactionResponse> getTransactionsWithFilters(TransactionFilterRequest filterRequest) {
        Pageable pageable = PageRequest.of(filterRequest.getPage(), filterRequest.getSize(), Sort.by("createdAt").descending());

        Page<Transaction> transactionPage = transactionRepository.findTransactionsWithFilters(
                filterRequest.getStatus(),
                filterRequest.getAccountNumber(),
                filterRequest.getStartDate(),
                filterRequest.getEndDate(),
                pageable
        );

        return transactionPage.map(TransactionResponse::fromTransaction);
    }

    @Transactional
    public void updateCommissionWorthy() {
        log.info("Starting scheduled job to update commission worthy transactions");

        List<Transaction> successfulTransactions = transactionRepository.findSuccessfulTransactionsWithoutCommission();
        BigDecimal totalCommission = BigDecimal.ZERO;
        BigDecimal commissionPercentage = new BigDecimal(commissionPercentageStr);

        for (Transaction transaction : successfulTransactions) {
            BigDecimal commission = transaction.getTransactionFee()
                    .multiply(commissionPercentage)
                    .setScale(2, RoundingMode.HALF_UP);

            transaction.setCommissionWorthy(true);
            transaction.setCommission(commission);

            totalCommission = totalCommission.add(commission);
            transactionRepository.save(transaction);
        }

        log.info("Updated {} transactions as commission worthy with total commission of {}",
                successfulTransactions.size(), totalCommission);
    }

    public TransactionSummaryResponse getTransactionSummary(LocalDate date) {
        Optional<TransactionSummary> existingSummary = transactionSummaryRepository.findByDate(date);

        if (existingSummary.isPresent()) {
            return TransactionSummaryResponse.fromTransactionSummary(existingSummary.get());
        }

        return generateTransactionSummary(date);
    }

    @Transactional
    public TransactionSummaryResponse generateTransactionSummary(LocalDate date) {
        log.info("Generating transaction summary for date: {}", date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Transaction> transactions = transactionRepository.findByCreatedAtBetween(startOfDay, endOfDay);

        TransactionSummary summary = buildTransactionSummary(date, transactions);
        TransactionSummary savedSummary = transactionSummaryRepository.save(summary);

        return TransactionSummaryResponse.fromTransactionSummary(savedSummary);
    }

    private BigDecimal calculateTransactionFee(BigDecimal amount) {
        BigDecimal feePercentage = new BigDecimal(feePercentageStr);
        BigDecimal feeCap = new BigDecimal(feeCapStr);

        BigDecimal fee = amount.multiply(feePercentage).setScale(2, RoundingMode.HALF_UP);
        return fee.compareTo(feeCap) > 0 ? feeCap : fee;
    }

    private Transaction buildInitialTransaction(TransferRequest request, String reference,
                                                BigDecimal transactionFee, BigDecimal billedAmount) {
        return Transaction.builder()
                .reference(reference)
                .sourceAccountNumber(request.getSourceAccountNumber())
                .destinationAccountNumber(request.getDestinationAccountNumber())
                .amount(request.getAmount())
                .transactionFee(transactionFee)
                .billedAmount(billedAmount)
                .description(request.getDescription())
                .status(TransactionStatus.PENDING)
                .commissionWorthy(false)
                .commission(BigDecimal.ZERO)
                .build();
    }

    private TransactionResponse handleAccountValidationFailure(Transaction transaction,
                                                               Optional<Account> sourceAccountOpt,
                                                               Optional<Account> destinationAccountOpt) {
        if (sourceAccountOpt.isEmpty()) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setStatusMessage("Source account not found");
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setStatusMessage("Destination account not found");
        }
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.warn("Account validation failed for transaction: {}", savedTransaction.getReference());
        return TransactionResponse.fromTransaction(savedTransaction);
    }

    private TransactionResponse handleInsufficientFunds(Transaction transaction) {
        transaction.setStatus(TransactionStatus.INSUFFICIENT_FUNDS);
        transaction.setStatusMessage("Insufficient funds in source account");
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.warn("Insufficient funds for transaction: {}", savedTransaction.getReference());
        return TransactionResponse.fromTransaction(savedTransaction);
    }

    private void processAccountBalances(Account sourceAccount, Account destinationAccount,
                                        BigDecimal billedAmount, BigDecimal transferAmount) {

        BigDecimal sourceBalance = sourceAccount.getBalance().subtract(billedAmount);
        sourceAccount.setBalance(sourceBalance);
        accountRepository.save(sourceAccount);
        log.info("Updated source account {} balance to {}", sourceAccount.getAccountNumber(), sourceBalance);


        BigDecimal destinationBalance = destinationAccount.getBalance().add(transferAmount);
        destinationAccount.setBalance(destinationBalance);
        accountRepository.save(destinationAccount);
        log.info("Updated destination account {} balance to {}", destinationAccount.getAccountNumber(), destinationBalance);
    }

    private TransactionResponse saveAndReturnResponse(Transaction transaction) {
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Saved transaction with reference: {}, status: {}", savedTransaction.getReference(), savedTransaction.getStatus());
        return TransactionResponse.fromTransaction(savedTransaction);
    }

    private void updateTransactionCommission(Transaction transaction) {
        if (transaction.isCommissionWorthy()) {
            log.debug("Transaction {} is already commission worthy, skipping", transaction.getReference());
            return;
        }

        BigDecimal commissionPercentage = new BigDecimal(commissionPercentageStr);
        BigDecimal commission = transaction.getTransactionFee()
                .multiply(commissionPercentage)
                .setScale(2, RoundingMode.HALF_UP);

        transaction.setCommissionWorthy(true);
        transaction.setCommission(commission);

        transactionRepository.save(transaction);
        log.debug("Updated transaction {} with commission {}", transaction.getReference(), commission);
    }

    private TransactionSummary buildTransactionSummary(LocalDate date, List<Transaction> transactions) {
        long totalTransactions = transactions.size();
        long successfulTransactions = countSuccessfulTransactions(transactions);
        long failedTransactions = totalTransactions - successfulTransactions;

        BigDecimal totalAmount = calculateTotalAmount(transactions);
        BigDecimal totalFees = calculateTotalFees(transactions);
        BigDecimal totalCommission = calculateTotalCommission(transactions);

        return TransactionSummary.builder()
                .date(date)
                .totalTransactions(totalTransactions)
                .successfulTransactions(successfulTransactions)
                .failedTransactions(failedTransactions)
                .totalAmount(totalAmount)
                .totalFees(totalFees)
                .totalCommission(totalCommission)
                .build();
    }

    private long countSuccessfulTransactions(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESSFUL)
                .count();
    }

    private BigDecimal calculateTotalAmount(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESSFUL)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalFees(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESSFUL)
                .map(Transaction::getTransactionFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalCommission(List<Transaction> transactions) {
        return transactions.stream()
                .filter(Transaction::isCommissionWorthy)
                .map(Transaction::getCommission)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}