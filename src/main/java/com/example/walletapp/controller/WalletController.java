package com.example.walletapp.controller;

import com.example.walletapp.dto.kafka.WalletOperationMessage;
import com.example.walletapp.dto.request.WalletRequest;
import com.example.walletapp.dto.response.WalletResponse;
import com.example.walletapp.dto.response.WalletTransactionResponse;
import com.example.walletapp.exception.TransactionNotFoundException;
import com.example.walletapp.exception.WalletException;
import com.example.walletapp.kafka.producer.KafkaSender;
import com.example.walletapp.model.WalletTransaction;
import com.example.walletapp.repository.WalletTransactionRepository;
import com.example.walletapp.service.WalletService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class WalletController {
    private final WalletService walletService;
    private final KafkaSender kafkaSender;
    private final WalletTransactionRepository walletTransactionRepository;

    @PostMapping("/wallet")
    public ResponseEntity<WalletTransactionResponse> processOperation(
            @Valid @RequestBody WalletRequest request
    ) {

        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(request.getWalletId())
                .amount(request.getAmount())
                .operationType(request.getOperationType())
                .status("PENDING").build();

        walletTransactionRepository.save(transaction);

        String message = serializeRequest(new WalletOperationMessage(
                transaction.getId(),
                request.getWalletId(),
                request.getOperationType(),
                request.getAmount()
        ));

        kafkaSender.sendMessage(message, "wallet");

        return ResponseEntity.ok(
                new WalletTransactionResponse(
                        transaction.getId(),
                        request.getWalletId(),
                        request.getAmount(),
                        "PROCESSING",
                        request.getOperationType()
                )
        );
    }

    @GetMapping("/wallets/{WALLET_UUID}")
    public ResponseEntity<WalletResponse> getBalance(@PathVariable UUID WALLET_UUID) {
        return ResponseEntity.ok(walletService.getBalance(WALLET_UUID));
    }

    private String serializeRequest(WalletOperationMessage request) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new WalletException("Ошибка сериализации запроса");
        }
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<WalletTransactionResponse> getTransactionStatus(
            @PathVariable Long transactionId) {

        WalletTransaction transaction = walletTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Транзакция не найдена"));

        return ResponseEntity.ok(
                new WalletTransactionResponse(
                        transaction.getId(),
                        transaction.getWalletId(),
                        transaction.getAmount(),
                        transaction.getStatus(),
                        transaction.getOperationType()
                )
        );
    }
}
