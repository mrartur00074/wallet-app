package com.example.walletapp.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class WalletOperationMessage {
    private Long transactionId;
    private UUID walletId;
    private String operationType;
    private Long amount;
}

