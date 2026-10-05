package com.dogukan.energy.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "A stored meter reading together with the applied tariff and calculated cost")
public record ConsumptionReadingResponse(

        @Schema(description = "Reading identifier", example = "120")
        Long id,

        @Schema(description = "Serial number of the meter", example = "SM-2024-0001")
        String meterSerialNumber,

        @Schema(description = "Name of the tariff applied to this reading", example = "Night Tariff")
        String tariffName,

        @Schema(description = "Price per kWh used in the calculation", example = "1.8500")
        BigDecimal unitPrice,

        @Schema(description = "Moment the reading was taken", example = "2026-09-28T14:00:00")
        LocalDateTime readingTime,

        @Schema(description = "Energy consumed during the hour, in kWh", example = "42.500")
        BigDecimal consumptionKwh,

        @Schema(description = "Calculated cost (consumption x unit price), rounded to 2 decimals", example = "78.63")
        BigDecimal totalCost
) {
}
