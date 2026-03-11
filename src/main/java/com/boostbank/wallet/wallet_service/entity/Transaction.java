package com.boostbank.wallet.wallet_service.entity;

import com.boostbank.wallet.wallet_service.enums.TransactionTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


/**
 * Represents a wallet transaction such as credit, debit, or transfer.
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    /**
     * Unique identifier for the transaction.
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Type of transaction (CREDIT, DEBIT, TRANSFER).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionTypeEnum type;

    /**
     * Amount involved in the transaction.
     */
    @Column(nullable = false)
    private BigDecimal amount;

    /**
     * Source user ID (for debit or transfer).
     */
    private UUID sourceUserId;

    /**
     * Destination user ID (for transfer).
     */
    private UUID destinationUserId;

    /**
     * Timestamp of when the transaction occurred.
     */
    @Column(nullable = false)
    private Instant timestamp;
}