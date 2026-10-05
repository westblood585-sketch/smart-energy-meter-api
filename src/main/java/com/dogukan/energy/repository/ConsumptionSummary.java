package com.dogukan.energy.repository;

import java.math.BigDecimal;

public interface ConsumptionSummary {

    BigDecimal getTotalConsumption();

    BigDecimal getTotalCost();

    Long getReadingCount();
}
