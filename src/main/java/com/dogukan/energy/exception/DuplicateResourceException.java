package com.dogukan.energy.exception;

public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String resource, String field, Object value) {
        super(ErrorCode.DUPLICATE_RESOURCE, "%s already exists with %s: %s".formatted(resource, field, value));
    }
}
