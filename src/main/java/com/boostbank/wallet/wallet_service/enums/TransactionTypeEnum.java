package com.boostbank.wallet.wallet_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * Represents the different types of wallet transactions supported
 * by the Digital Wallet Microservice.
 *
 * <p>
 * CREDIT   - Funds added to a user's wallet (top-up).
 * DEBIT    - Funds deducted from a user's wallet (payment).
 * TRANSFER - Funds moved between two users.
 * </p>
 */
@Getter
@AllArgsConstructor
public enum TransactionTypeEnum {

    /**
     * Credit transaction where funds are added to the user's wallet.
     */
    CREDIT("CREDIT", "Represents a credit operation where funds are added to the wallet"),

    /**
     * Debit transaction where funds are deducted from the user's wallet.
     */
    DEBIT("DEBIT", "Represents a debit operation where funds are deducted from the wallet"),

    /**
     * Transfer transaction where funds are moved from one user to another.
     */
    TRANSFER("TRANSFER", "Represents a transfer of funds between two users");

    /**
     * Machine-readable transaction code.
     */
    private final String code;

    /**
     * Human-readable description of the transaction type.
     */
    private final String desc;

    /**
     * Retrieves the TransactionTypeEnum corresponding to the given code.
     *
     * @param code the transaction type code
     * @return the matching TransactionTypeEnum, or null if no match is found
     */
    public static TransactionTypeEnum getByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        for (TransactionTypeEnum transactionTypeEnum : TransactionTypeEnum.values()) {
            if (transactionTypeEnum.getCode().equalsIgnoreCase(code)) {
                return transactionTypeEnum;
            }
        }

        return null;
    }
}
