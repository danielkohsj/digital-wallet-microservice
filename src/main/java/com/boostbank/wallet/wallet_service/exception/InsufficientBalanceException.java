package com.boostbank.wallet.wallet_service.exception;


/**
 * Exception thrown when a wallet does not have sufficient balance
 * to perform a debit or transfer operation.
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
