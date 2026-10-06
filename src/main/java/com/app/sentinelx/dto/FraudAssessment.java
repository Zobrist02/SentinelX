package com.app.sentinelx.dto;

import com.app.sentinelx.entity.FraudDecision;

public class FraudAssessment {

    private Long transactionId;
    private int fraudScore;
    private FraudDecision decision;

    public FraudAssessment() {
    }

    public FraudAssessment(Long transactionId, int fraudScore,
                           FraudDecision decision) {
        this.transactionId = transactionId;
        this.fraudScore = fraudScore;
        this.decision = decision;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public int getFraudScore() {
        return fraudScore;
    }

    public void setFraudScore(int fraudScore) {
        this.fraudScore = fraudScore;
    }

    public FraudDecision getDecision() {
        return decision;
    }

    public void setDecision(FraudDecision decision) {
        this.decision = decision;
    }
}