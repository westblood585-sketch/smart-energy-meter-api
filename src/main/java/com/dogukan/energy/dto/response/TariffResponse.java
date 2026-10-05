package com.dogukan.energy.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Price tariff details")
public record TariffResponse(

        @Schema(description = "Tariff identifier", example = "1")
        Long id,

        @Schema(description = "Tariff name", example = "Night Tariff")
        String name,

        @Schema(description = "Price per kWh", example = "1.8500")
        BigDecimal unitPrice,

        @Schema(description = "First valid day (inclusive)", example = "2026-01-01")
        LocalDate validFrom,

        @Schema(description = "Last valid day (inclusive), empty when open-ended", example = "2026-12-31", nullable = true)
        LocalDate validTo,

        @Schema(description = "Daily window start (inclusive)", example = "22:00", type = "string", format = "time")
        LocalTime startTime,

        @Schema(description = "Daily window end (exclusive)", example = "06:00", type = "string", format = "time")
        LocalTime endTime,

        @Schema(description = "Whether the tariff can be applied to new readings", example = "true")
        boolean active
) {
}
