package com.notfound.timetrackcommon.exception;

import com.notfound.timetrackcommon.api.ResultCode;

public class BizException extends RuntimeException {

    private final ResultCode resultCode;

    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }
}

