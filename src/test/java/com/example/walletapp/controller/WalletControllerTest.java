package com.example.walletapp.controller;

import com.example.walletapp.dto.kafka.WalletOperationMessage;
import com.example.walletapp.dto.request.WalletRequest;
import com.example.walletapp.dto.response.WalletResponse;
import com.example.walletapp.dto.response.WalletTransactionResponse;
import com.example.walletapp.exception.*;
import com.example.walletapp.kafka.producer.KafkaSender;
import com.example.walletapp.model.WalletTransaction;
import com.example.walletapp.repository.WalletTransactionRepository;
import com.example.walletapp.service.WalletService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class WalletControllerTest {

    @Mock
    private WalletService walletService;
    @Mock
    private KafkaSender kafkaSender;
    @Mock
    private WalletTransactionRepository transactionRepository;
    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MockMvc mockMvc;

    @InjectMocks
    private WalletController walletController;

    private UUID testWalletId;
    private WalletRequest validDepositRequest;
    private WalletResponse successResponse;

    @BeforeEach
    void setUp() {
        testWalletId = UUID.randomUUID();

        validDepositRequest = new WalletRequest();
        validDepositRequest.setWalletId(testWalletId);
        validDepositRequest.setOperationType("DEPOSIT");
        validDepositRequest.setAmount(1000L);

        successResponse = new WalletResponse(testWalletId, 1500L);

        mockMvc = MockMvcBuilders.standaloneSetup(walletController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void processOperation_ShouldAcceptRequestAndReturnTransactionId() {
        when(transactionRepository.save(any(WalletTransaction.class)))
                .thenAnswer(invocation -> {
                    WalletTransaction t = invocation.getArgument(0);
                    t.setId(1L);
                    return t;
                });

        ResponseEntity<WalletTransactionResponse> response = walletController.processOperation(validDepositRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTransactionId());
        verify(transactionRepository).save(any(WalletTransaction.class));
    }

    @Test
    void processOperation_ShouldSetCorrectInitialStatus() {
        when(transactionRepository.save(any(WalletTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        walletController.processOperation(validDepositRequest);

        ArgumentCaptor<WalletTransaction> transactionCaptor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());

        WalletTransaction savedTransaction = transactionCaptor.getValue();
        assertEquals("PENDING", savedTransaction.getStatus());
        assertEquals(testWalletId, savedTransaction.getWalletId());
        assertEquals(1000L, savedTransaction.getAmount());
    }

    @Test
    void getTransactionStatus_ShouldReturnStatus() {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setStatus("PROCESSING");
        when(transactionRepository.findById(anyLong()))
                .thenReturn(Optional.of(transaction));

        ResponseEntity<WalletTransactionResponse> response =
                walletController.getTransactionStatus(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("PROCESSING", response.getBody().getStatus());
    }

    /*@Test
    void getTransactionStatus_NotFound_ShouldReturn404() throws Exception {
        when(transactionRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> {
            walletController.getTransactionStatus(1L);
        });

        mockMvc.perform(get("/api/v1/transactions/1"))
                .andExpect(status().isNotFound());
    }*/

    @Test
    void getBalance_ValidWallet_ShouldReturnOk() {
        when(walletService.getBalance(testWalletId)).thenReturn(successResponse);

        ResponseEntity<WalletResponse> response = walletController.getBalance(testWalletId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(successResponse, response.getBody());
        verify(walletService, times(1)).getBalance(testWalletId);
    }
}