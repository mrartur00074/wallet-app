package com.example.walletapp.service.Impl;

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

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public WalletResponse processOperation(WalletRequest request) {
        log.info("Начало обработки операции: {}", request);
        int retryCount = 0;
        while (retryCount < 3) {
            try {
                log.debug("Попытка {} для операции {}", retryCount + 1, request.getOperationType());
                Wallet wallet = walletRepository.findByIdWithLock(request.getWalletId())
                        .orElseThrow(() -> {
                            log.error("Кошелек не найден: {}", request.getWalletId());
                            return new WalletNotFoundException(request.getWalletId());
                        });

                log.debug("Текущий баланс кошелька {}: {}", wallet.getId(), wallet.getBalance());

                switch (request.getOperationType()) {
                    case "DEPOSIT":
                        log.debug("Пополнение на сумму: {}", request.getAmount());
                        wallet.deposit(request.getAmount());
                        break;
                    case "WITHDRAW":
                        log.debug("Списание суммы: {}", request.getAmount());
                        wallet.withdraw(request.getAmount());
                        break;
                    default:
                        log.error("Неизвестный тип операции: {}", request.getOperationType());
                        throw new InvalidOperationException("Неизвестная операция");
                }

                walletRepository.save(wallet);
                log.info("Успешное выполнение операции. Новый баланс: {}", wallet.getBalance());
                return new WalletResponse(wallet.getId(), wallet.getBalance());
            } catch (ObjectOptimisticLockingFailureException | PessimisticLockingFailureException ex) {
                retryCount++;
                log.warn("Конфликт блокировки (попытка {}). Ошибка: {}", retryCount, ex.getMessage());
                if (retryCount == 3) {
                    log.error("Достигнуто максимальное количество попыток (3) для операции {}", request);
                    throw new OperationConflictException("Не удалось выполнить операцию после 3 попыток");
                }
                log.error("Недопустимое состояние после обработки операции: {}", request);
                sleepExponentially(retryCount);
            }
        }

        throw new IllegalStateException("Недопустимое состояние");
    }

    private void sleepExponentially(int retryCount) {
        long sleepTime = (long) Math.pow(2, retryCount) * 50;
        log.debug("Ожидание перед повторной попыткой: {} мс", sleepTime);
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException ignored) {
            log.warn("Прерывание во время ожидания повторной попытки");
            Thread.currentThread().interrupt();
        }
    }

    public WalletResponse getBalance(UUID walletId) {
        log.debug("Запрос баланса для кошелька: {}", walletId);

        if (walletId == null) {
            log.error("Получен пустой walletId");
            throw new IllegalArgumentException("Wallet UUID пустой");
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> {
                    log.error("Кошелек не найден: {}", walletId);
                    return new WalletNotFoundException(walletId);
                });

        log.info("Текущий баланс кошелька {}: {}", walletId, wallet.getBalance());
        return new WalletResponse(wallet.getId(), wallet.getBalance());
    }
}
