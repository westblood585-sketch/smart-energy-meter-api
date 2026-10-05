package com.dogukan.energy.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Standard error body returned by every failing endpoint")
public record ErrorResponse(

        @Schema(description = "Moment the error was produced (UTC)", example = "2026-09-28T11:30:00Z")
        Instant timestamp,

        @Schema(description = "HTTP status code", example = "422")
        int status,

        @Schema(description = "HTTP reason phrase", example = "Unprocessable Entity")
        String error,

        @Schema(description = "Stable machine-readable error code", example = "ANOMALOUS_READING")
        String code,

        @Schema(description = "Human-readable explanation", example = "Reading 300 kWh for meter SM-2024-0001 is at least 3 times the previous reading of 90 kWh")
        String message,

        @Schema(description = "Request path that caused the error", example = "/api/v1/readings")
        String path,

        @Schema(description = "Per-field validation details, empty for non-validation errors")
        List<FieldErrorDetail> fieldErrors
) {
}
