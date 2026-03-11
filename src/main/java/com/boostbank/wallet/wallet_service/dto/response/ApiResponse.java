package com.boostbank.wallet.wallet_service.dto.response;

import com.boostbank.wallet.wallet_service.enums.ResultInfo;
import lombok.Builder;
import lombok.Data;


/**
 * Generic wrapper for all API responses returned by the wallet service.
 *
 * <p>This class standardizes the structure of responses sent by the API,
 * ensuring that every response contains a result status and optional
 * response payload data.</p>
 *
 * @param <T> the type of payload returned in the response
 */
@Data
@Builder
public class ApiResponse<T> {

    /**
     * Indicates whether the API request was successful or failed.
     */
    private ResultInfo result;

    /**
     * Optional message providing additional information about the response.
     */
    private String message;

    /**
     * The payload returned by the API.
     * May be null if the request does not return data.
     */
    private T data;

}