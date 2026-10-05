package com.dogukan.energy.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Aggregated consumption and cost for a meter or a zone over a date range")
public record ConsumptionReportResponse(

        @Schema(description = "Identifier of the reported meter or zone", example = "1")
        Long referenceId,

        @Schema(description = "Serial number of the meter or name of the zone", example = "Production Hall")
        String referenceName,

        @Schema(description = "Range start (inclusive)", example = "2026-09-01T00:00:00")
        LocalDateTime from,

        @Schema(description = "Range end (inclusive)", example = "2026-09-30T23:59:59")
        LocalDateTime to,

        @Schema(description = "Total consumption in kWh, zero when there are no readings", example = "10450.750")
        BigDecimal totalConsumptionKwh,

        @Schema(description = "Total cost, zero when there are no readings", example = "19333.89")
        BigDecimal totalCost,

        @Schema(description = "Number of readings in the range", example = "720")
        long readingCount
) {
}
