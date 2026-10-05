package com.dogukan.energy.exception;

public abstract class BusinessException extends RuntimeException {

    private final transient ErrorCode code;

    protected BusinessException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}
