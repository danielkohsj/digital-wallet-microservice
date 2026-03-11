package com.boostbank.wallet.wallet_service.dto.response;

import com.boostbank.wallet.wallet_service.enums.ResultInfo;
import com.boostbank.wallet.wallet_service.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response object representing a wallet transaction.
 */
@Data
@Builder
public class TransactionResponse {

    /**
     * Unique identifier of the transaction.
     */
    private UUID id;

    /**
     * The type of transaction performed.
     */
    private TransactionType type;

    /**
     * The monetary amount involved in the transaction.
     */
    private BigDecimal amount;

    /**
     * The source user initiating the transaction.
     */
    private UUID sourceUserId;

    /**
     * The destination user receiving the transaction.
     */
    private UUID destinationUserId;

    /**
     * Timestamp indicating when the transaction occurred.
     */
    private Instant timestamp;

}