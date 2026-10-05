package com.dogukan.energy.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Payload for submitting one hourly meter reading")
public record ConsumptionReadingRequest(

        @Schema(description = "Identifier of the meter that produced the reading", example = "1")
        @NotNull(message = "Meter id is required")
        Long meterId,

        @Schema(description = "Moment the reading was taken", example = "2026-09-28T14:00:00")
        @NotNull(message = "Reading time is required")
        @PastOrPresent(message = "Reading time must not be in the future")
        LocalDateTime readingTime,

        @Schema(description = "Energy consumed during the hour, in kWh", example = "42.500")
        @NotNull(message = "Consumption is required")
        @PositiveOrZero(message = "Consumption must not be negative")
        @Digits(integer = 9, fraction = 3, message = "Consumption allows up to 9 integer and 3 fraction digits")
        BigDecimal consumptionKwh
) {
}
