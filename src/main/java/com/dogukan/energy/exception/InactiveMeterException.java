package com.dogukan.energy.exception;

import com.dogukan.energy.entity.MeterStatus;

public class InactiveMeterException extends BusinessException {

    public InactiveMeterException(String serialNumber, MeterStatus status) {
        super(ErrorCode.INACTIVE_METER,
                "Meter %s is %s and cannot accept readings".formatted(serialNumber, status));
    }
}
