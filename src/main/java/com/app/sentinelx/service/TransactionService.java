package com.app.sentinelx.service;

import com.app.sentinelx.dto.FraudAssessment;
import com.app.sentinelx.dto.TransactionAssessmentResponse;
import com.app.sentinelx.dto.TransactionRequest;
import com.app.sentinelx.entity.Transaction;
import com.app.sentinelx.kafka.TransactionEventProducer;
import com.app.sentinelx.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TransactionService {

    private final FraudDetectionService fraudDetectionService;
    private final TransactionRepository transactionRepository;
    private final TransactionEventProducer transactionEventProducer;

    public TransactionService(
            TransactionRepository transactionRepository,
            FraudDetectionService fraudDetectionService,
            TransactionEventProducer transactionEventProducer) {

        this.transactionRepository = transactionRepository;
        this.fraudDetectionService = fraudDetectionService;
        this.transactionEventProducer = transactionEventProducer;
    }

    public TransactionAssessmentResponse createTransaction(
            TransactionRequest request) {

        Transaction transaction = new Transaction();

        transaction.setUserId(request.getUserId());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setMerchant(request.getMerchant());
        transaction.setLocation(request.getLocation());
        transaction.setTimestamp(LocalDateTime.now());

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        FraudAssessment assessment =
                fraudDetectionService.assess(savedTransaction);

        transactionEventProducer.publish(
                savedTransaction,
                assessment
        );

        return new TransactionAssessmentResponse(
                savedTransaction.getId(),
                savedTransaction.getUserId(),
                savedTransaction.getAmount(),
                savedTransaction.getCurrency(),
                savedTransaction.getMerchant(),
                savedTransaction.getLocation(),
                savedTransaction.getTimestamp(),
                assessment.getFraudScore(),
                assessment.getDecision()
        );
    }
}