package com.example.walletapp.repository;

import com.example.walletapp.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    @Transactional
    @Modifying
    @Query("UPDATE WalletTransaction t SET t.status = :status WHERE t.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    @Transactional
    @Modifying
    @Query("UPDATE WalletTransaction t SET t.status = :status, t.errorMessage = :error WHERE t.id = :id")
    void updateStatusAndError(@Param("id") Long id, @Param("status") String status, @Param("error") String error);
}
