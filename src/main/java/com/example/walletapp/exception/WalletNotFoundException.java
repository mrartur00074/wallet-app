package com.example.walletapp.exception;

import java.util.UUID;

public class WalletNotFoundException extends WalletException {
    public WalletNotFoundException(UUID walletId) {
        super("Кошелёк не существует: " + walletId);
    }
}
