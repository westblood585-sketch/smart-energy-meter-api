package com.dogukan.energy.controller;

import com.dogukan.energy.dto.request.ConsumptionReadingRequest;
import com.dogukan.energy.dto.response.ConsumptionReadingResponse;
import com.dogukan.energy.dto.response.ConsumptionReportResponse;
import com.dogukan.energy.dto.response.ErrorResponse;
import com.dogukan.energy.service.ConsumptionReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/readings")
@Tag(name = "Consumption Readings", description = "Hourly meter readings and consumption reports")
public class ConsumptionReadingController {

    private final ConsumptionReadingService readingService;

    public ConsumptionReadingController(ConsumptionReadingService readingService) {
        this.readingService = readingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit an hourly meter reading",
            description = "Validates meter status and consumption anomalies, resolves the applicable tariff "
                    + "and calculates the total cost before persisting the reading.")
    @ApiResponse(responseCode = "201", description = "Reading accepted and cost calculated")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Meter not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description =
            "Meter is inactive/faulty, the reading is an anomalous spike, or no tariff applies",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ConsumptionReadingResponse submit(@Valid @RequestBody ConsumptionReadingRequest request) {
        return readingService.submit(request);
    }

    @GetMapping("/meters/{meterId}")
    @Operation(summary = "List readings for a meter", description = "Returns a paginated, time-filtered reading history")
    @ApiResponse(responseCode = "404", description = "Meter not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Page<ConsumptionReadingResponse> getByMeter(
            @PathVariable Long meterId,
            @Parameter(description = "Range start (inclusive), defaults to 30 days ago")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "Range end (inclusive), defaults to now")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 50) Pageable pageable) {
        LocalDateTime resolvedTo = to != null ? to : LocalDateTime.now();
        LocalDateTime resolvedFrom = from != null ? from : resolvedTo.minusDays(30);
        return readingService.getByMeter(meterId, resolvedFrom, resolvedTo, pageable);
    }

    @GetMapping("/meters/{meterId}/report")
    @Operation(summary = "Get a consumption report for a meter",
            description = "Aggregated total consumption, total cost and reading count over a date range")
    @ApiResponse(responseCode = "404", description = "Meter not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ConsumptionReportResponse getMeterReport(
            @PathVariable Long meterId,
            @Parameter(description = "Range start (inclusive), defaults to 30 days ago")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "Range end (inclusive), defaults to now")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        LocalDateTime resolvedTo = to != null ? to : LocalDateTime.now();
        LocalDateTime resolvedFrom = from != null ? from : resolvedTo.minusDays(30);
        return readingService.getMeterReport(meterId, resolvedFrom, resolvedTo);
    }

    @GetMapping("/zones/{zoneId}/report")
    @Operation(summary = "Get a consumption report for a zone",
            description = "Aggregated total consumption, total cost and reading count for every meter in the zone")
    @ApiResponse(responseCode = "404", description = "Zone not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ConsumptionReportResponse getZoneReport(
            @PathVariable Long zoneId,
            @Parameter(description = "Range start (inclusive), defaults to 30 days ago")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "Range end (inclusive), defaults to now")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        LocalDateTime resolvedTo = to != null ? to : LocalDateTime.now();
        LocalDateTime resolvedFrom = from != null ? from : resolvedTo.minusDays(30);
        return readingService.getZoneReport(zoneId, resolvedFrom, resolvedTo);
    }
}
