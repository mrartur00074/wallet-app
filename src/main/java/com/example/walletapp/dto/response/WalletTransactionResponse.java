package com.example.walletapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WalletTransactionResponse {
    private Long transactionId;
    private UUID walletId;
    private Long amount;
    private String status;
    private String operationType;
}
