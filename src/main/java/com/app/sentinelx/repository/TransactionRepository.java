package com.app.sentinelx.repository;

import com.app.sentinelx.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
       SELECT COUNT(t)
       FROM Transaction t
       WHERE t.userId = :userId
       AND t.timestamp >= :since
       """)
    long countRecentTransactions(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since);

    @Query("""
       SELECT t
       FROM Transaction t
       WHERE t.userId = :userId
       AND t.id <> :transactionId
       ORDER BY t.timestamp DESC
       LIMIT 1
       """)
    Optional<Transaction> findLatestPreviousTransaction(
            @Param("userId") Long userId,
            @Param("transactionId") Long transactionId);
}