package com.app.sentinelx.controller;

import com.app.sentinelx.entity.FraudAssessmentEntity;
import com.app.sentinelx.exception.FraudAssessmentNotFoundException;
import com.app.sentinelx.repository.FraudAssessmentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fraud-assessments")
public class FraudAssessmentController {

    private final FraudAssessmentRepository fraudAssessmentRepository;

    public FraudAssessmentController(
            FraudAssessmentRepository fraudAssessmentRepository) {

        this.fraudAssessmentRepository = fraudAssessmentRepository;
    }

    @GetMapping
    public List<FraudAssessmentEntity> getAllAssessments() {
        return fraudAssessmentRepository.findAllByOrderByAssessedAtDesc();
    }

    @GetMapping("/{transactionId}")
    public FraudAssessmentEntity getAssessment(
            @PathVariable Long transactionId) {

        return fraudAssessmentRepository
                .findByTransactionId(transactionId)
                .orElseThrow(() -> new FraudAssessmentNotFoundException(
                "Fraud assessment not found for transaction: " + transactionId
        ));
    }
}