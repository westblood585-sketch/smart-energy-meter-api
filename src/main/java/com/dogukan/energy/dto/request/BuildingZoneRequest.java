package com.dogukan.energy.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for creating or updating a building zone")
public record BuildingZoneRequest(

        @Schema(description = "Unique zone name", example = "Production Hall", maxLength = 100)
        @NotBlank(message = "Zone name must not be blank")
        @Size(max = 100, message = "Zone name must be at most 100 characters")
        String name,

        @Schema(description = "Optional free-text description", example = "Main manufacturing floor", maxLength = 255)
        @Size(max = 255, message = "Description must be at most 255 characters")
        String description
) {
}
