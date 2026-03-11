package com.boostbank.wallet.wallet_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;


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
     * Timestamp when the request was processed.
     */
    private Instant createdAt;
}
