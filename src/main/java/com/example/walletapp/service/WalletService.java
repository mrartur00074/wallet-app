package com.example.walletapp.service;

import com.example.walletapp.dto.request.WalletRequest;

import java.util.UUID;

public interface WalletService {
    void processOperation(WalletRequest request);
    Long getBalance(UUID walletId);
}
