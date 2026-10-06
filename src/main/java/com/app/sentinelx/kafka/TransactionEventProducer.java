package com.app.sentinelx.kafka;

import com.app.sentinelx.dto.FraudAssessment;
import com.app.sentinelx.entity.Transaction;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventProducer {

    private static final String TOPIC = "transaction-assessed-events";

    private final KafkaTemplate<String, TransactionAssessedEvent> kafkaTemplate;

    public TransactionEventProducer(
            KafkaTemplate<String, TransactionAssessedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(
            Transaction transaction,
            FraudAssessment assessment) {

        TransactionAssessedEvent event =
                new TransactionAssessedEvent(
                        transaction.getId(),
                        transaction.getUserId(),
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        transaction.getMerchant(),
                        transaction.getLocation(),
                        transaction.getTimestamp(),
                        assessment.getFraudScore(),
                        assessment.getDecision()
                );

        kafkaTemplate.send(
                TOPIC,
                transaction.getId().toString(),
                event
        );
    }
}