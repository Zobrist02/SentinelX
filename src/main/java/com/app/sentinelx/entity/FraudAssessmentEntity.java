package com.app.sentinelx.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_assessments")
public class FraudAssessmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long transactionId;

    @Column(nullable = false)
    private int fraudScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudDecision decision;

    @Column(nullable = false)
    private LocalDateTime assessedAt;

    public FraudAssessmentEntity() {
    }

    public FraudAssessmentEntity(
            Long transactionId,
            int fraudScore,
            FraudDecision decision,
            LocalDateTime assessedAt) {

        this.transactionId = transactionId;
        this.fraudScore = fraudScore;
        this.decision = decision;
        this.assessedAt = assessedAt;
    }

    public Long getId() {
        return id;
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

    public LocalDateTime getAssessedAt() {
        return assessedAt;
    }

    public void setAssessedAt(LocalDateTime assessedAt) {
        this.assessedAt = assessedAt;
    }
}