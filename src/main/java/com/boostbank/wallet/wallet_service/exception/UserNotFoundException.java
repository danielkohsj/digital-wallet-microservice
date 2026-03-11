package com.boostbank.wallet.wallet_service.exception;


/**
 * Exception thrown when the requested user is not found in the database.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
