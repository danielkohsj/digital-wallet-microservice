package com.boostbank.wallet.wallet_service.dto.response;

import com.boostbank.wallet.wallet_service.enums.ResultInfo;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;


/**
 * Response payload containing a user's wallet balance.
 */
@Data
@Builder
public class BalanceResponse {

    /**
     * The current balance of the user's wallet.
     */
    private BigDecimal balance;

}