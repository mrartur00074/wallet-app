package com.example.walletapp.service.Impl;

import com.example.walletapp.dto.request.WalletRequest;
import com.example.walletapp.dto.response.WalletResponse;
import com.example.walletapp.exception.InsufficientFundsException;
import com.example.walletapp.exception.InvalidOperationException;
import com.example.walletapp.exception.OperationConflictException;
import com.example.walletapp.exception.WalletNotFoundException;
import com.example.walletapp.model.Wallet;
import com.example.walletapp.repository.WalletRepository;
import com.example.walletapp.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public WalletResponse processOperation(WalletRequest request) {
        int retryCount = 0;
        while (retryCount < 3) {
            try {
                Wallet wallet = walletRepository.findByIdWithLock(request.getWalletId())
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
                return new WalletResponse(wallet.getId(), wallet.getBalance());
            } catch (ObjectOptimisticLockingFailureException | PessimisticLockingFailureException ex) {
                retryCount++;
                if (retryCount == 3) {
                    throw new OperationConflictException("Не удалось выполнить операцию после 3 попыток");
                }
                sleepExponentially(retryCount);
            }
        }

        throw new IllegalStateException("Недопустимое состояние");
    }

    private void sleepExponentially(int retryCount) {
        try {
            Thread.sleep((long) Math.pow(2, retryCount) * 50);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    public WalletResponse getBalance(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        return new WalletResponse(wallet.getId(), wallet.getBalance());
    }
}
