package com.app.sentinelx.service.fraud;

import com.app.sentinelx.entity.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HighAmountRule implements FraudRule {

    private static final BigDecimal HIGH_AMOUNT =
            new BigDecimal("100000");

    @Override
    public int evaluate(Transaction transaction) {

        if (transaction.getAmount().compareTo(HIGH_AMOUNT) > 0) {
            return 40;
        }

        return 0;
    }
}