package com.notfound.timetrackcommon.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApiResponseTest {

    @Test
    void successWithDataReturnsOkCodeAndPayload() {
        ApiResponse<String> response = ApiResponse.success("ok-data");

        assertEquals(ResultCode.SUCCESS.getCode(), response.getCode());
        assertEquals(ResultCode.SUCCESS.getMessage(), response.getMessage());
        assertEquals("ok-data", response.getData());
    }

    @Test
    void failReturnsExpectedErrorCodeAndMessage() {
        ApiResponse<Void> response = ApiResponse.fail(ResultCode.NOT_FOUND, "user not found");

        assertEquals(ResultCode.NOT_FOUND.getCode(), response.getCode());
        assertEquals("user not found", response.getMessage());
        assertNull(response.getData());
    }
}

