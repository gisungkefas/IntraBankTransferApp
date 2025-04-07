package com.example.MoneyTransferApp.dto;

import com.example.MoneyTransferApp.enums.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionFilterRequest {

    private TransactionStatus status;

    @Pattern(regexp = "\\d{10}", message = "Account number must be a 10-digit number")
    private String accountNumber;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    @Min(value = 1, message = "Page number cannot be negative")
    private int page = 1;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot be greater than 100")
    private int size = 10;
}
