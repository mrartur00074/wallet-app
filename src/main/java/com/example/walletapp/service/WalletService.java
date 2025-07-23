package com.example.walletapp.service;

import com.example.walletapp.dto.kafka.WalletOperationMessage;
import com.example.walletapp.dto.request.WalletRequest;
import com.example.walletapp.dto.response.WalletResponse;

import java.util.UUID;

public interface WalletService {
    void processOperation(WalletOperationMessage request);
    WalletResponse getBalance(UUID walletId);
}
