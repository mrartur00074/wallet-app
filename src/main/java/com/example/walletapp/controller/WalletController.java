package com.example.walletapp.controller;

import com.example.walletapp.dto.request.WalletRequest;
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
    public ResponseEntity<Void> processOperation(
            @Valid @RequestBody WalletRequest request
    ) {
        walletService.processOperation(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{WALLET_UUID}")
    public ResponseEntity<Long> getBalance(@PathVariable UUID WALLET_UUID) {
        return ResponseEntity.ok(walletService.getBalance(WALLET_UUID));
    }
}
