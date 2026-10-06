package com.app.sentinelx.service.fraud;

import com.app.sentinelx.entity.Transaction;
import com.app.sentinelx.repository.TransactionRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class VelocityRule implements FraudRule {

    private static final int REVIEW_THRESHOLD = 6;
    private static final int HIGH_VELOCITY_THRESHOLD = 9;

    private final TransactionRepository transactionRepository;

    public VelocityRule(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public int evaluate(Transaction transaction) {

        LocalDateTime since =
                transaction.getTimestamp().minusMinutes(5);

        long recentTransactions =
                transactionRepository.countRecentTransactions(
                        transaction.getUserId(),
                        since);

        if (recentTransactions >= HIGH_VELOCITY_THRESHOLD) {
            return 50;
        }

        if (recentTransactions >= REVIEW_THRESHOLD) {
            return 30;
        }

        return 0;
    }
}