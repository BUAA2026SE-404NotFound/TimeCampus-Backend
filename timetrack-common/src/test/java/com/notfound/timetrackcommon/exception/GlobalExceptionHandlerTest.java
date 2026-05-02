package com.notfound.timetrackcommon.exception;

import com.notfound.timetrackcommon.api.ApiResponse;
import com.notfound.timetrackcommon.api.ResultCode;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBizExceptionReturnsBizCodeAndMessage() {
        BizException ex = new BizException(ResultCode.BIZ_ERROR, "biz failed");

        ApiResponse<Void> response = handler.handleBizException(ex);

        assertEquals(ResultCode.BIZ_ERROR.getCode(), response.getCode());
        assertEquals("biz failed", response.getMessage());
    }

    @Test
    void handleBindExceptionReturnsFormattedFieldErrors() {
        BindException bindException = new BindException(new Object(), "request");
        bindException.addError(new FieldError("request", "code", "must not be blank"));

        ApiResponse<Void> response = handler.handleBindException(bindException);

        assertEquals(ResultCode.VALIDATION_ERROR.getCode(), response.getCode());
        assertEquals("code: must not be blank", response.getMessage());
    }

    @Test
    void handleConstraintViolationExceptionReturnsValidationCode() {
        ConstraintViolationException ex = new ConstraintViolationException("invalid param", Collections.emptySet());

        ApiResponse<Void> response = handler.handleConstraintViolationException(ex);

        assertEquals(ResultCode.VALIDATION_ERROR.getCode(), response.getCode());
        assertEquals("invalid param", response.getMessage());
    }

    @Test
    void handleExceptionReturnsInternalErrorCode() {
        ApiResponse<Void> response = handler.handleException(new RuntimeException("boom"));

        assertEquals(ResultCode.INTERNAL_ERROR.getCode(), response.getCode());
        assertEquals(ResultCode.INTERNAL_ERROR.getMessage(), response.getMessage());
    }
}
