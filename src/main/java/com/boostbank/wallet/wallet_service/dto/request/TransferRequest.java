package com.boostbank.wallet.wallet_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;


/**
 * Request payload used to transfer funds between two wallet users.
 */
@Data
public class TransferRequest {

    /**
     * The unique identifier of the user sending the funds.
     */
    private UUID sourceUserId;

    /**
     * The unique identifier of the user receiving the funds.
     */
    private UUID destinationUserId;

    /**
     * The amount to transfer between the wallets.
     */
    private BigDecimal amount;

}