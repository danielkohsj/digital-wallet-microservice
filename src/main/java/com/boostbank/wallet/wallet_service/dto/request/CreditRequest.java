package com.boostbank.wallet.wallet_service.dto.request;

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
    private UUID userId;

    /**
     * The amount to be credited to the wallet.
     */
    private BigDecimal amount;

}