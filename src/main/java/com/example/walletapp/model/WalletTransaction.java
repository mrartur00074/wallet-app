package com.example.walletapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "wallet_transactions")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID walletId;

    @Column(nullable = false)
    private String operationType;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private String status;

    @Column
    private String errorMessage;
}