package com.boostbank.wallet.wallet_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * Request payload used to create a new wallet user.
 */
@Data
public class CreateUserRequest {

    /**
     * The full name of the user.
     */
    @NotBlank
    private String name;

    /**
     * Unique email address associated with the user.
     */
    @NotBlank
    private String email;

}