package com.example.walletapp.controller;

import com.example.walletapp.dto.request.WalletRequest;
import com.example.walletapp.dto.response.WalletResponse;
import com.example.walletapp.exception.*;
import com.example.walletapp.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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

    private MockMvc mockMvc;

    @InjectMocks
    private WalletController walletController;

    private UUID testWalletId;
    private WalletRequest validDepositRequest;
    private WalletRequest validWithdrawRequest;
    private WalletRequest invalidOperationRequest;
    private WalletResponse successResponse;

    @BeforeEach
    void setUp() {
        testWalletId = UUID.randomUUID();

        validDepositRequest = new WalletRequest();
        validDepositRequest.setWalletId(testWalletId);
        validDepositRequest.setOperationType("DEPOSIT");
        validDepositRequest.setAmount(1000L);

        validWithdrawRequest = new WalletRequest();
        validWithdrawRequest.setWalletId(testWalletId);
        validWithdrawRequest.setOperationType("WITHDRAW");
        validWithdrawRequest.setAmount(500L);

        invalidOperationRequest = new WalletRequest();
        invalidOperationRequest.setWalletId(testWalletId);
        invalidOperationRequest.setOperationType("UNKNOWN");
        invalidOperationRequest.setAmount(100L);

        successResponse = new WalletResponse(testWalletId, 1500L);
        mockMvc = MockMvcBuilders.standaloneSetup(walletController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void processOperation_Deposit_ShouldReturnOk() {
        when(walletService.processOperation(validDepositRequest)).thenReturn(successResponse);

        ResponseEntity<WalletResponse> response = walletController.processOperation(validDepositRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(successResponse, response.getBody());
        verify(walletService, times(1)).processOperation(validDepositRequest);
    }

    @Test
    void processOperation_Withdraw_ShouldReturnOk() {
        when(walletService.processOperation(validWithdrawRequest)).thenReturn(successResponse);

        ResponseEntity<WalletResponse> response = walletController.processOperation(validWithdrawRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(successResponse, response.getBody());
        verify(walletService, times(1)).processOperation(validWithdrawRequest);
    }

    @Test
    void processOperation_WalletNotFound_ShouldThrow() {
        when(walletService.processOperation(any(WalletRequest.class)))
                .thenThrow(new WalletNotFoundException(testWalletId));

        assertThrows(WalletNotFoundException.class, () -> {
            walletController.processOperation(validDepositRequest);
        });
    }

    @Test
    void processOperation_InsufficientFunds_ShouldThrow() {
        when(walletService.processOperation(any(WalletRequest.class)))
                .thenThrow(new InsufficientFundsException(testWalletId, 10000000L));

        assertThrows(InsufficientFundsException.class, () -> {
            walletController.processOperation(validWithdrawRequest);
        });
    }

    @Test
    void processOperation_InvalidOperation_ShouldThrow() {
        when(walletService.processOperation(invalidOperationRequest))
                .thenThrow(new InvalidOperationException("Неизвестная операция"));

        assertThrows(InvalidOperationException.class, () -> {
            walletController.processOperation(invalidOperationRequest);
        });
    }

    @Test
    void processOperation_OperationConflict_ShouldThrow() {
        when(walletService.processOperation(any(WalletRequest.class)))
                .thenThrow(new OperationConflictException("Не удалось выполнить операцию после 3 попыток"));

        assertThrows(OperationConflictException.class, () -> {
            walletController.processOperation(validDepositRequest);
        });
    }

    @Test
    void getBalance_ValidWallet_ShouldReturnOk() {
        when(walletService.getBalance(testWalletId)).thenReturn(successResponse);

        ResponseEntity<WalletResponse> response = walletController.getBalance(testWalletId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(successResponse, response.getBody());
        verify(walletService, times(1)).getBalance(testWalletId);
    }

    @Test
    void getBalance_WalletNotFound_ShouldThrow() {
        when(walletService.getBalance(testWalletId))
                .thenThrow(new WalletNotFoundException(testWalletId));

        assertThrows(WalletNotFoundException.class, () -> {
            walletController.getBalance(testWalletId);
        });
    }

    @Test
    void getBalance_InvalidUUID_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/invalid_uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBalance_ValidUUID_ShouldCallService() throws Exception {
        UUID validUUID = UUID.randomUUID();
        when(walletService.getBalance(validUUID)).thenReturn(new WalletResponse(validUUID, 100L));

        mockMvc.perform(get("/api/v1/wallets/" + validUUID))
                .andExpect(status().isOk());

        verify(walletService).getBalance(validUUID);
    }
}