package com.app.sentinelx.service.fraud;

import com.app.sentinelx.entity.Transaction;
import com.app.sentinelx.repository.TransactionRepository;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class LocationChangeRule implements FraudRule {

    private static final long MINUTES_THRESHOLD = 10;

    private final TransactionRepository transactionRepository;

    public LocationChangeRule(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public int evaluate(Transaction transaction) {

        Optional<Transaction> previousTransaction =
                transactionRepository.findLatestPreviousTransaction(
                        transaction.getUserId(),
                        transaction.getId());

        if (previousTransaction.isEmpty()) {
            return 0;
        }

        Transaction previous = previousTransaction.get();

        boolean locationChanged =
                !previous.getLocation()
                        .equalsIgnoreCase(transaction.getLocation());

        long minutesBetween =
                Duration.between(
                        previous.getTimestamp(),
                        transaction.getTimestamp()
                ).toMinutes();

        if (locationChanged && minutesBetween <= MINUTES_THRESHOLD) {
            return 40;
        }

        return 0;
    }
}