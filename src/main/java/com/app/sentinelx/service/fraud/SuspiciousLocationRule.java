package com.app.sentinelx.service.fraud;

import com.app.sentinelx.entity.Transaction;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SuspiciousLocationRule implements FraudRule {

    private static final Set<String> SUSPICIOUS_LOCATIONS =
            Set.of("UNKNOWN", "HIGH_RISK");

    @Override
    public int evaluate(Transaction transaction) {

        if (SUSPICIOUS_LOCATIONS.contains(
                transaction.getLocation().toUpperCase())) {
            return 30;
        }

        return 0;
    }
}