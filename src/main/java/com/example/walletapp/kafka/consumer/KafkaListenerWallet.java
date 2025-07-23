package com.example.walletapp.kafka.consumer;

import com.example.walletapp.dto.kafka.WalletOperationMessage;
import com.example.walletapp.dto.request.WalletRequest;
import com.example.walletapp.model.WalletTransaction;
import com.example.walletapp.repository.WalletTransactionRepository;
import com.example.walletapp.service.WalletService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaListenerWallet {
    private final WalletService walletService;
    private final ObjectMapper objectMapper;
    private final WalletTransactionRepository walletTransactionRepository;

    @KafkaListener(topics = "wallet", groupId = "group1")
    public void processWalletOperation(String message, Acknowledgment acknowledgment) {
        try {
            WalletOperationMessage request = objectMapper.readValue(message, WalletOperationMessage.class);

            if (request.getTransactionId() == null) {
                log.error("TransactionId is missing in message: {}", message);
                return;
            }

            walletTransactionRepository.updateStatus(request.getTransactionId(), "PROCESSING");

            walletService.processOperation(request);

            walletTransactionRepository.updateStatus(request.getTransactionId(), "SUCCESS");
            log.info("Transaction {} processed successfully", request.getTransactionId());
            acknowledgment.acknowledge();

        } catch (JsonProcessingException e) {
            log.error("Failed to parse message: {}. Error: {}", message, e.getMessage());
            acknowledgment.acknowledge();
        } catch (Exception e) {
            try {
                WalletOperationMessage failedRequest = objectMapper.readValue(message, WalletOperationMessage.class);
                if (failedRequest.getTransactionId() != null) {
                    walletTransactionRepository.updateStatusAndError(
                            failedRequest.getTransactionId(),
                            "ERROR",
                            e.getMessage()
                    );
                    log.error("Failed to process transaction {}: {}", failedRequest.getTransactionId(), e.getMessage());
                    acknowledgment.acknowledge();
                }
            } catch (JsonProcessingException jsonEx) {
                log.error("Failed to parse message for error handling: {}", message);
                acknowledgment.acknowledge();
            }
        }
    }
}
