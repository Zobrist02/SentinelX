package com.app.sentinelx.service;

import com.app.sentinelx.dto.FraudAssessment;
import com.app.sentinelx.entity.FraudAssessmentEntity;
import com.app.sentinelx.entity.FraudDecision;
import com.app.sentinelx.entity.Transaction;
import com.app.sentinelx.repository.FraudAssessmentRepository;
import com.app.sentinelx.service.fraud.FraudRule;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FraudDetectionService {

    private final List<FraudRule> fraudRules;
    private final FraudAssessmentRepository fraudAssessmentRepository;

    public FraudDetectionService(
            List<FraudRule> fraudRules,
            FraudAssessmentRepository fraudAssessmentRepository) {

        this.fraudRules = fraudRules;
        this.fraudAssessmentRepository = fraudAssessmentRepository;
    }

    public FraudAssessment assess(Transaction transaction) {

        int fraudScore = fraudRules.stream()
                .mapToInt(rule -> rule.evaluate(transaction))
                .sum();

        FraudDecision decision = determineDecision(fraudScore);

        FraudAssessmentEntity assessmentEntity =
                new FraudAssessmentEntity(
                        transaction.getId(),
                        fraudScore,
                        decision,
                        LocalDateTime.now()
                );

        fraudAssessmentRepository.save(assessmentEntity);

        return new FraudAssessment(
                transaction.getId(),
                fraudScore,
                decision
        );
    }

    private FraudDecision determineDecision(int score) {

        if (score >= 70) {
            return FraudDecision.BLOCK;
        }

        if (score >= 40) {
            return FraudDecision.REVIEW;
        }

        return FraudDecision.APPROVE;
    }
}