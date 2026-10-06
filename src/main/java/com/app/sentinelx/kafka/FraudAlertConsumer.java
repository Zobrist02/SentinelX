package com.app.sentinelx.kafka;

import com.app.sentinelx.entity.FraudDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FraudAlertConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(FraudAlertConsumer.class);

    @KafkaListener(
            topics = "transaction-assessed-events",
            groupId = "sentinelx-alerts"
    )
    public void consume(TransactionAssessedEvent event) {

        if (event.getDecision() == FraudDecision.REVIEW
                || event.getDecision() == FraudDecision.BLOCK) {

            log.warn(
                    "FRAUD ALERT: transactionId={}, userId={}, " +
                            "fraudScore={}, decision={}, merchant={}, amount={}",
                    event.getTransactionId(),
                    event.getUserId(),
                    event.getFraudScore(),
                    event.getDecision(),
                    event.getMerchant(),
                    event.getAmount()
            );
        }
    }
}