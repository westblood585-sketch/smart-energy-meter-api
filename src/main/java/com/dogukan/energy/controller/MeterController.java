package com.dogukan.energy.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dogukan.energy.dto.request.MeterRequest;
import com.dogukan.energy.dto.response.ErrorResponse;
import com.dogukan.energy.dto.response.MeterResponse;
import com.dogukan.energy.service.MeterService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meters")
@Tag(name = "Meters", description = "Smart energy meters installed in building zones")
public class MeterController {
    private static final Logger log = LoggerFactory.getLogger(MeterController.class);


    private final MeterService meterService;

    public MeterController(MeterService meterService) {
        this.meterService = meterService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new meter")
    @ApiResponse(responseCode = "201", description = "Meter created")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Zone not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "A meter with the same serial number already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public MeterResponse create(@Valid @RequestBody MeterRequest request) {
        log.info("Executing MeterController#create");
        return meterService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a meter by id")
    @ApiResponse(responseCode = "200", description = "Meter found")
    @ApiResponse(responseCode = "404", description = "Meter not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public MeterResponse getById(@PathVariable Long id) {
        log.info("Executing MeterController#getById");
        return meterService.getById(id);
    }

    @GetMapping
    @Operation(summary = "List meters",
            description = "Returns a paginated list of meters, optionally filtered by zone")
    @ApiResponse(responseCode = "404", description = "Zone not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Page<MeterResponse> getAll(
            @Parameter(description = "Optional zone id filter") @RequestParam(required = false) Long zoneId,
            @PageableDefault(size = 20) Pageable pageable) {
        return zoneId == null ? meterService.getAll(pageable) : meterService.getByZone(zoneId, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a meter")
    @ApiResponse(responseCode = "200", description = "Meter updated")
    @ApiResponse(responseCode = "404", description = "Meter or zone not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "A meter with the same serial number already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public MeterResponse update(@PathVariable Long id, @Valid @RequestBody MeterRequest request) {
        log.info("Executing MeterController#update");
        return meterService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a meter")
    @ApiResponse(responseCode = "204", description = "Meter deleted")
    @ApiResponse(responseCode = "404", description = "Meter not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Executing MeterController#delete");
        meterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
