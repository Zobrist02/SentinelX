package com.app.sentinelx.kafka;

import com.app.sentinelx.entity.FraudDecision;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionAssessedEvent {

    private Long transactionId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String merchant;
    private String location;
    private LocalDateTime timestamp;
    private int fraudScore;
    private FraudDecision decision;

    public TransactionAssessedEvent() {
    }

    public TransactionAssessedEvent(
            Long transactionId,
            Long userId,
            BigDecimal amount,
            String currency,
            String merchant,
            String location,
            LocalDateTime timestamp,
            int fraudScore,
            FraudDecision decision) {

        this.transactionId = transactionId;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.merchant = merchant;
        this.location = location;
        this.timestamp = timestamp;
        this.fraudScore = fraudScore;
        this.decision = decision;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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