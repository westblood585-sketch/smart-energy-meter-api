package com.dogukan.energy.exception;

import java.math.BigDecimal;

public class AnomalousReadingException extends BusinessException {

    public AnomalousReadingException(String serialNumber, BigDecimal previous, BigDecimal current, int multiplier) {
        super(ErrorCode.ANOMALOUS_READING,
                "Reading %s kWh for meter %s is at least %d times the previous reading of %s kWh"
                        .formatted(current.toPlainString(), serialNumber, multiplier, previous.toPlainString()));
    }
}
