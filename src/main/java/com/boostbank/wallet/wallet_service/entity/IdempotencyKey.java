package com.boostbank.wallet.wallet_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "idempotency_keys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyKey {

    /**
     * Unique idempotency key provided by the client.
     */
    @Id
    private String idempotencyKey;

    /**
     * Transaction ID associated with the processed request.
     */
    private UUID transactionId;

    /**
     * Timestamp when the request was processed.
     */
    private Instant createdAt;
}
