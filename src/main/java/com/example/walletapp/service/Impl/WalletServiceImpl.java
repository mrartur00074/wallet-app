package com.example.walletapp.service.Impl;

import com.example.walletapp.dto.request.WalletRequest;
import com.example.walletapp.exception.InsufficientFundsException;
import com.example.walletapp.exception.InvalidOperationException;
import com.example.walletapp.exception.WalletNotFoundException;
import com.example.walletapp.model.Wallet;
import com.example.walletapp.repository.WalletRepository;
import com.example.walletapp.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;

    @Transactional
    public void processOperation(WalletRequest request) {
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException(request.getWalletId()));

        switch (request.getOperationType()) {
            case DEPOSIT:
                wallet.setBalance(wallet.getBalance() + request.getAmount());
                walletRepository.save(wallet);
            case WITHDRAW:
                if (wallet.getBalance() < request.getAmount()) {
                    throw new InsufficientFundsException(request.getWalletId(), request.getAmount());
                }
                wallet.setBalance(wallet.getBalance() - request.getAmount());
                walletRepository.save(wallet);
            default:
                throw new InvalidOperationException("Неизвестная операция");
        }
    }

    public Long getBalance(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new NoSuchElementException("Счёт не найден"))
                .getBalance();
    }
}
