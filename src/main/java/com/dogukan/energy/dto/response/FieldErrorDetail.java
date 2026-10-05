package com.dogukan.energy.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Validation problem tied to a single request field")
public record FieldErrorDetail(

        @Schema(description = "Name of the invalid field", example = "consumptionKwh")
        String field,

        @Schema(description = "Why the value was rejected", example = "Consumption must not be negative")
        String message
) {
}
