package com.dogukan.energy.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dogukan.energy.dto.request.BuildingZoneRequest;
import com.dogukan.energy.dto.response.BuildingZoneResponse;
import com.dogukan.energy.dto.response.ErrorResponse;
import com.dogukan.energy.service.BuildingZoneService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/zones")
@Tag(name = "Building Zones", description = "Facility zones that group smart meters")
public class BuildingZoneController {
    private static final Logger log = LoggerFactory.getLogger(BuildingZoneController.class);


    private final BuildingZoneService zoneService;

    public BuildingZoneController(BuildingZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a building zone")
    @ApiResponse(responseCode = "201", description = "Zone created")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "A zone with the same name already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public BuildingZoneResponse create(@Valid @RequestBody BuildingZoneRequest request) {
        log.info("Executing BuildingZoneController#create");
        return zoneService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a building zone by id")
    @ApiResponse(responseCode = "200", description = "Zone found")
    @ApiResponse(responseCode = "404", description = "Zone not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public BuildingZoneResponse getById(@PathVariable Long id) {
        log.info("Executing BuildingZoneController#getById");
        return zoneService.getById(id);
    }

    @GetMapping
    @Operation(summary = "List building zones", description = "Returns a paginated list of all building zones")
    public Page<BuildingZoneResponse> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return zoneService.getAll(pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a building zone")
    @ApiResponse(responseCode = "200", description = "Zone updated")
    @ApiResponse(responseCode = "404", description = "Zone not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "A zone with the same name already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public BuildingZoneResponse update(@PathVariable Long id, @Valid @RequestBody BuildingZoneRequest request) {
        log.info("Executing BuildingZoneController#update");
        return zoneService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a building zone", description = "Also deletes every meter installed in the zone")
    @ApiResponse(responseCode = "204", description = "Zone deleted")
    @ApiResponse(responseCode = "404", description = "Zone not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> delete(@Parameter(description = "Zone id") @PathVariable Long id) {
        zoneService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
