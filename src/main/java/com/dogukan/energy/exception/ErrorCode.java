package com.dogukan.energy.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT),
    DATA_CONFLICT(HttpStatus.CONFLICT),
    INACTIVE_METER(HttpStatus.UNPROCESSABLE_ENTITY),
    ANOMALOUS_READING(HttpStatus.UNPROCESSABLE_ENTITY),
    TARIFF_NOT_FOUND(HttpStatus.UNPROCESSABLE_ENTITY),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST),
    ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
