package com.boostbank.wallet.wallet_service.exception;

import com.boostbank.wallet.wallet_service.dto.response.BaseResponse;
import com.boostbank.wallet.wallet_service.enums.ResultInfo;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<Void> handleRuntimeException(RuntimeException ex) {

        return BaseResponse.<Void>builder()
                .result(ResultInfo.FAILED)
                .message(ex.getMessage())
                .build();
    }

}
