package com.dogukan.energy.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Payload for creating or updating a price tariff")
public record TariffRequest(

        @Schema(description = "Tariff name", example = "Night Tariff", maxLength = 100)
        @NotBlank(message = "Tariff name must not be blank")
        @Size(max = 100, message = "Tariff name must be at most 100 characters")
        String name,

        @Schema(description = "Price per kWh", example = "1.8500")
        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.0001", message = "Unit price must be greater than zero")
        @Digits(integer = 6, fraction = 4, message = "Unit price allows up to 6 integer and 4 fraction digits")
        BigDecimal unitPrice,

        @Schema(description = "First day the tariff is valid (inclusive)", example = "2026-01-01")
        @NotNull(message = "Valid-from date is required")
        LocalDate validFrom,

        @Schema(description = "Last day the tariff is valid (inclusive). Empty means open-ended", example = "2026-12-31", nullable = true)
        LocalDate validTo,

        @Schema(description = "Start of the daily time window (inclusive)", example = "22:00", type = "string", format = "time")
        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @Schema(description = "End of the daily time window (exclusive). A value before the start time crosses midnight; equal values cover the whole day",
                example = "06:00", type = "string", format = "time")
        @NotNull(message = "End time is required")
        LocalTime endTime,

        @Schema(description = "Whether the tariff can currently be applied to new readings", example = "true")
        @NotNull(message = "Active flag is required")
        Boolean active
) {

    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "Valid-to date must not be before valid-from date")
    public boolean isValidDateRange() {
        return validFrom == null || validTo == null || !validTo.isBefore(validFrom);
    }
}
