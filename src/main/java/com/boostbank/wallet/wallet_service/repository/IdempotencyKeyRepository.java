package com.boostbank.wallet.wallet_service.repository;

import com.boostbank.wallet.wallet_service.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;


/**
 * Repository storing idempotent keys that are unique to each transaction.
 * Used to prevent duplicate transactions.
 */
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {

    @Modifying
    @Query(value = "INSERT INTO idempotency_keys (idempotency_key, created_at) VALUES (:key, :createdAt)", nativeQuery = true)
    void insertKey(@Param("key") String key, @Param("createdAt") Instant createdAt);
}
