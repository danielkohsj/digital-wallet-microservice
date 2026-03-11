package com.boostbank.wallet.wallet_service.exception;


/**
 * Exception thrown when a request with the same idempotency key
 * has already been processed.
 */
public class DuplicateRequestException extends RuntimeException {

    public DuplicateRequestException(String message) {
        super(message);
    }
}
