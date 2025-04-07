package com.example.MoneyTransferApp.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotBlank(message = "Source account number is required")
    @Pattern(regexp = "\\d{10}", message = "Source account number must be exactly 10 digits")
    private String sourceAccountNumber;

    @NotBlank(message = "Destination account number is required")
    @Pattern(regexp = "\\d{10}", message = "Destination account number must be exactly 10 digits")
    private String destinationAccountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 150, message = "Description must not exceed 255 characters")
    private String description;
}
