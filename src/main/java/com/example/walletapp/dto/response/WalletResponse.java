package com.example.walletapp.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WalletResponse {
    private UUID walletId;
    private Long amount;
}
