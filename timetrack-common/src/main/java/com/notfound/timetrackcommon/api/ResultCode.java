package com.notfound.timetrackcommon.api;

public enum ResultCode {
    SUCCESS(0, "ok"),
    TOO_MANY_REQUESTS(429, "request too many"),
    VALIDATION_ERROR(4001, "request validation failed"),
    BIZ_ERROR(4002, "business error"),
    UNAUTHORIZED(4010, "unauthorized"),
    FORBIDDEN(4030, "forbidden"),
    NOT_FOUND(4004, "resource not found"),
    INTERNAL_ERROR(5000, "internal server error");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

