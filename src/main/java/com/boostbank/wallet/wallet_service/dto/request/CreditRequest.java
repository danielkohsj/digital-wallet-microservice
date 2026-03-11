package com.boostbank.wallet.wallet_service.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;


/**
 * Request payload used to credit funds into a user's wallet.
 */
@Data
public class CreditRequest {

    /**
     * The unique identifier of the user receiving the credit.
     */
    @NotNull
    private UUID userId;

    /**
     * The amount to be credited to the wallet.
     */
    @NotNull
    @Positive
    private BigDecimal amount;

}