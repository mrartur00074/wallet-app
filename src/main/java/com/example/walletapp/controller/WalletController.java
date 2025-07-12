package com.example.walletapp.controller;

import com.example.walletapp.dto.request.WalletRequest;
import com.example.walletapp.dto.response.WalletResponse;
import com.example.walletapp.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/wallet")
    public ResponseEntity<WalletResponse> processOperation(
            @Valid @RequestBody WalletRequest request
    ) {

        return ResponseEntity.ok(walletService.processOperation(request));
    }

    @GetMapping("/wallets/{WALLET_UUID}")
    public ResponseEntity<WalletResponse> getBalance(@PathVariable UUID WALLET_UUID) {
        return ResponseEntity.ok(walletService.getBalance(WALLET_UUID));
    }
}
