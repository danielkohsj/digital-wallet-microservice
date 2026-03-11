package com.boostbank.wallet.wallet_service.dto.response;

import com.boostbank.wallet.wallet_service.enums.ResultInfo;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;


/**
 * Response object returned when user information is retrieved or created.
 */
@Data
@Builder
public class UserResponse {

    /**
     * Unique identifier of the user.
     */
    private UUID id;

    /**
     * The user's full name.
     */
    private String name;

    /**
     * The user's email address.
     */
    private String email;

    /**
     * Current wallet balance of the user.
     */
    private BigDecimal balance;

}