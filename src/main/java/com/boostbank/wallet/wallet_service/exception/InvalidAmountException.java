package com.boostbank.wallet.wallet_service.exception;


/**
 * Exception thrown when a transaction amount is invalid.
 */
public class InvalidAmountException extends RuntimeException {

    public InvalidAmountException(String message) {
        super(message);
    }
}
