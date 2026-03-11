package com.boostbank.wallet.wallet_service.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;


/**
 * Request payload used to debit funds from a user's wallet.
 */
@Data
public class DebitRequest {

    /**
     * The unique identifier of the user whose wallet will be debited.
     */
    @NotNull
    private UUID userId;

    /**
     * The amount to deduct from the wallet.
     */
    @NotNull
    @Positive
    private BigDecimal amount;

}