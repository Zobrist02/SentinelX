package com.app.sentinelx.repository;

import com.app.sentinelx.entity.FraudAssessmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FraudAssessmentRepository extends JpaRepository<FraudAssessmentEntity, Long> {

    Optional<FraudAssessmentEntity> findByTransactionId(Long transactionId);
    List<FraudAssessmentEntity> findAllByOrderByAssessedAtDesc();
}