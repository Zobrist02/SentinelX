package com.app.sentinelx.service.fraud;

import com.app.sentinelx.entity.Transaction;

public interface FraudRule {

    int evaluate(Transaction transaction);
}