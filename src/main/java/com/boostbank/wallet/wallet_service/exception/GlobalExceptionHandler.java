package com.boostbank.wallet.wallet_service.exception;

import com.boostbank.wallet.wallet_service.dto.response.BaseResponse;
import com.boostbank.wallet.wallet_service.enums.ResultInfo;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for the wallet service.
 *
 * <p>This class centralizes exception handling across all REST controllers.
 * It intercepts runtime exceptions thrown during request processing and
 * converts them into standardized API responses using {@link BaseResponse}.</p>
 *
 * <p>The purpose of this handler is to ensure that all errors returned by the
 * API follow a consistent structure rather than exposing raw stack traces
 * or default framework error messages.</p>
 *
 * <p>Typical scenarios handled include:</p>
 * <ul>
 *     <li>Insufficient wallet balance</li>
 *     <li>Duplicate idempotency requests</li>
 *     <li>User not found errors</li>
 *     <li>Invalid transaction amounts</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles all {@link RuntimeException} occurrences thrown within the application.
     *
     * <p>When a runtime exception occurs during request processing, this method
     * captures the exception and returns a standardized API response with:</p>
     *
     * <ul>
     *     <li>{@link ResultInfo#FAILED} status</li>
     *     <li>The exception message describing the error</li>
     * </ul>
     *
     * <p>This ensures that clients receive a consistent error response format.</p>
     *
     * @param ex the runtime exception thrown by the application
     * @return a standardized API error response
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<Void> handleRuntimeException(RuntimeException ex) {

        return BaseResponse.<Void>builder()
                .result(ResultInfo.FAILED)
                .message(ex.getMessage())
                .build();
    }
}
