package com.boostbank.wallet.wallet_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;


/**
 * Represents a wallet user in the digital wallet system.
 * Each user has a unique email and a wallet balance.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Unique identifier for the user.
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * User's full name.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Unique email address used to identify the user.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Current wallet balance.
     * BigDecimal is used to prevent floating-point precision errors.
     */
    @Column(nullable = false)
    private BigDecimal balance;
}