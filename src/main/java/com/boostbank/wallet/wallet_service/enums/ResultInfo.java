package com.boostbank.wallet.wallet_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the outcome status of an API response.
 *
 * <p>This enum is used to standardize the result state returned by
 * wallet service APIs.</p>
 *
 * <ul>
 *     <li>SUCCESS - The request was processed successfully.</li>
 *     <li>FAILED - The request failed due to validation or business logic errors.</li>
 *     <li>UNKNOWN - The request outcome could not be determined due to unexpected errors.</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public enum ResultInfo {

    /**
     * Indicates that the API request was processed successfully.
     */
    SUCCESS("SUCCESS", "The request was processed successfully."),

    /**
     * Indicates that the API request failed due to validation errors
     * or business rule violations.
     */
    FAILED("FAILED", "The request failed due to validation or business logic errors"),

    /**
     * Indicates that the API request was processed successfully.
     */
    UNKNOWN("UNKNOWN", "The request outcome could not be determined due to unexpected errors.");

    /**
     * Machine-readable code representing the result status.
     */
    private final String code;

    /**
     * Human-readable description explaining the result status.
     */
    private final String desc;

    /**
     * Retrieves the ResultInfo corresponding to the given code.
     *
     * @param code the result info code
     * @return the matching ResultInfo, or null if no match is found
     */
    public static ResultInfo getByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        for (ResultInfo resultInfo : ResultInfo.values()) {
            if (resultInfo.getCode().equalsIgnoreCase(code)) {
                return resultInfo;
            }
        }

        return null;
    }
}
