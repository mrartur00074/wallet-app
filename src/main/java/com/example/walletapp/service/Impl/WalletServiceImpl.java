package com.example.walletapp.service.Impl;

import com.example.walletapp.dto.kafka.WalletOperationMessage;
import com.example.walletapp.dto.request.WalletRequest;
import com.example.walletapp.dto.response.WalletResponse;
import com.example.walletapp.exception.InvalidOperationException;
import com.example.walletapp.exception.OperationConflictException;
import com.example.walletapp.exception.WalletNotFoundException;
import com.example.walletapp.model.Wallet;
import com.example.walletapp.repository.WalletRepository;
import com.example.walletapp.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;

    @Transactional
    public void processOperation(WalletOperationMessage request) {
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException(request.getWalletId()));

        switch (request.getOperationType()) {
            case "DEPOSIT":
                wallet.deposit(request.getAmount());
                break;
            case "WITHDRAW":
                wallet.withdraw(request.getAmount());
                break;
            default:
                throw new InvalidOperationException("Неизвестная операция");
        }
        walletRepository.save(wallet);
    }

    public WalletResponse getBalance(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        return new WalletResponse(wallet.getId(), wallet.getBalance());
    }
}
