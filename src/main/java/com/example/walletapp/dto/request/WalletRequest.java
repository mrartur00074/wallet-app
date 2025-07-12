package com.example.walletapp.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletRequest {
    @NotNull
    private UUID walletId;

    @NotNull
    private String operationType;

    @Positive(message = "Сумма должна быть положительной")
    @Min(value = 1, message = "Сумма должна быть не менее 1")
    private Long amount;
}
