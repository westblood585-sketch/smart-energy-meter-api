package com.dogukan.energy.exception;

import java.time.LocalDateTime;

public class TariffNotFoundException extends BusinessException {

    public TariffNotFoundException(LocalDateTime readingTime) {
        super(ErrorCode.TARIFF_NOT_FOUND, "No active tariff covers reading time " + readingTime);
    }
}
