package com.example.walletapp.config;
import com.example.walletapp.model.Wallet;
import com.example.walletapp.repository.WalletRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class DataInitializer {
    private final WalletRepository walletRepository;

    private static final UUID WALLET_1_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID WALLET_2_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @PostConstruct
    @Transactional
    public void init() {
        List<UUID> testWalletIds = Arrays.asList(WALLET_1_UUID, WALLET_2_UUID);
        boolean walletsExist = walletRepository.findAllById(testWalletIds).size() == 2;

        if (!walletsExist) {
            Wallet wallet1 = new Wallet();
            wallet1.setId(WALLET_1_UUID);
            wallet1.setBalance(1000L);
            walletRepository.save(wallet1);

            Wallet wallet2 = new Wallet();
            wallet2.setId(WALLET_2_UUID);
            wallet2.setBalance(100L);
            walletRepository.save(wallet2);
        }
    }
}
