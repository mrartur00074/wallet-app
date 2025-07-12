package com.example.walletapp.service;

import com.example.walletapp.dto.request.WalletRequest;
import com.example.walletapp.dto.response.WalletResponse;

import java.util.UUID;

public interface WalletService {
    WalletResponse processOperation(WalletRequest request);
    WalletResponse getBalance(UUID walletId);
}
