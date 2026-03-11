package com.boostbank.wallet.wallet_service.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    @NotNull
    private UUID sourceUserId;

    /**
     * The unique identifier of the user receiving the funds.
     */
    @NotNull
    private UUID destinationUserId;

    /**
     * The amount to transfer between the wallets.
     */
    @NotNull
    @Positive
    private BigDecimal amount;

}