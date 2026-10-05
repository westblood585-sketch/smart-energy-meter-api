package com.dogukan.energy.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Building zone details")
public record BuildingZoneResponse(

        @Schema(description = "Zone identifier", example = "1")
        Long id,

        @Schema(description = "Zone name", example = "Production Hall")
        String name,

        @Schema(description = "Zone description", example = "Main manufacturing floor", nullable = true)
        String description
) {
}
