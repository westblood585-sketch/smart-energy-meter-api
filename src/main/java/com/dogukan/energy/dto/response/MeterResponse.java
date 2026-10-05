package com.dogukan.energy.dto.response;

import com.dogukan.energy.entity.MeterStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Smart meter details")
public record MeterResponse(

        @Schema(description = "Meter identifier", example = "1")
        Long id,

        @Schema(description = "Device serial number", example = "SM-2024-0001")
        String serialNumber,

        @Schema(description = "Operational status", example = "ACTIVE")
        MeterStatus status,

        @Schema(description = "Identifier of the zone the meter belongs to", example = "1")
        Long zoneId,

        @Schema(description = "Name of the zone the meter belongs to", example = "Production Hall")
        String zoneName
) {
}
