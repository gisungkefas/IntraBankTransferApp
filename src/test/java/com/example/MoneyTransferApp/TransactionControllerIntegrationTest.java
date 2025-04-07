package com.example.MoneyTransferApp;

import com.example.MoneyTransferApp.controller.TransactionController;
import com.example.MoneyTransferApp.dto.*;
import com.example.MoneyTransferApp.enums.TransactionStatus;
import com.example.MoneyTransferApp.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
public class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @Test
    void processTransfer_Success() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber("1234567890");
        request.setDestinationAccountNumber("0987654321");
        request.setAmount(new BigDecimal("1000"));
        request.setDescription("Test transfer");

        TransactionResponse response = TransactionResponse.builder()
                .id(UUID.randomUUID().toString())
                .reference(UUID.randomUUID().toString())
                .sourceAccountNumber("1234567890")
                .destinationAccountNumber("0987654321")
                .amount(new BigDecimal("1000"))
                .transactionFee(new BigDecimal("5.00"))
                .billedAmount(new BigDecimal("1005.00"))
                .status(TransactionStatus.SUCCESSFUL)
                .statusMessage("Transfer completed successfully")
                .build();

        when(transactionService.processTransfer(any(TransferRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESSFUL"))
                .andExpect(jsonPath("$.sourceAccountNumber").value("1234567890"))
                .andExpect(jsonPath("$.destinationAccountNumber").value("0987654321"));
    }

    @Test
    void processTransfer_ValidationError() throws Exception {
        TransferRequest request = new TransferRequest();

        mockMvc.perform(post("/api/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTransactions_Success() throws Exception {
        TransactionResponse response1 = TransactionResponse.builder()
                .id(UUID.randomUUID().toString())
                .status(TransactionStatus.SUCCESSFUL)
                .build();

        TransactionResponse response2 = TransactionResponse.builder()
                .id(UUID.randomUUID().toString())
                .status(TransactionStatus.FAILED)
                .build();

        List<TransactionResponse> transactionList = List.of(response1, response2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<TransactionResponse> pageResponse = new PageImpl<>(transactionList, pageable, transactionList.size());

        when(transactionService.getTransactionsWithFilters(any(TransactionFilterRequest.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/transactions")
                        .param("status", "SUCCESSFUL")
                        .param("accountNumber", "1234567890")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getTransactionSummary_Success() throws Exception {
        LocalDate date = LocalDate.now();

        TransactionSummaryResponse response = TransactionSummaryResponse.builder()
                .date(date)
                .totalTransactions(10)
                .successfulTransactions(8)
                .failedTransactions(2)
                .totalAmount(new BigDecimal("5000.00"))
                .totalFees(new BigDecimal("25.00"))
                .totalCommission(new BigDecimal("5.00"))
                .build();

        when(transactionService.getTransactionSummary(any(LocalDate.class))).thenReturn(response);

        mockMvc.perform(get("/api/transactions/summary")
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTransactions").value(10))
                .andExpect(jsonPath("$.successfulTransactions").value(8));
    }
}
