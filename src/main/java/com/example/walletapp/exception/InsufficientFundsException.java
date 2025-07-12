package com.example.walletapp.exception;

import java.util.UUID;

public class InsufficientFundsException extends WalletException {
    public InsufficientFundsException(UUID walletId, Long amount) {
      super(String.format("Недостаточно средств %s. Необходимо: %d", walletId, amount));
    }
}
