package com.dogukan.energy.dto.request;

import com.dogukan.energy.entity.MeterStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for creating or updating a smart meter")
public record MeterRequest(

        @Schema(description = "Unique serial number printed on the device", example = "SM-2024-0001", maxLength = 50)
        @NotBlank(message = "Serial number must not be blank")
        @Size(max = 50, message = "Serial number must be at most 50 characters")
        String serialNumber,

        @Schema(description = "Operational status. Only ACTIVE meters accept readings", example = "ACTIVE")
        @NotNull(message = "Status is required")
        MeterStatus status,

        @Schema(description = "Identifier of the building zone the meter is installed in", example = "1")
        @NotNull(message = "Zone id is required")
        Long zoneId
) {
}
