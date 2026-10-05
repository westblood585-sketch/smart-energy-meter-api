package com.dogukan.energy.controller;

import com.dogukan.energy.dto.request.TariffRequest;
import com.dogukan.energy.dto.response.ErrorResponse;
import com.dogukan.energy.dto.response.TariffResponse;
import com.dogukan.energy.service.TariffService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/tariffs")
@Tag(name = "Tariffs", description = "Time-based electricity price tariffs")
public class TariffController {

    private final TariffService tariffService;

    public TariffController(TariffService tariffService) {
        this.tariffService = tariffService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a price tariff")
    @ApiResponse(responseCode = "201", description = "Tariff created")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public TariffResponse create(@Valid @RequestBody TariffRequest request) {
        return tariffService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a tariff by id")
    @ApiResponse(responseCode = "200", description = "Tariff found")
    @ApiResponse(responseCode = "404", description = "Tariff not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public TariffResponse getById(@PathVariable Long id) {
        return tariffService.getById(id);
    }

    @GetMapping
    @Operation(summary = "List tariffs", description = "Returns a paginated list of all tariffs")
    public Page<TariffResponse> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return tariffService.getAll(pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a tariff")
    @ApiResponse(responseCode = "200", description = "Tariff updated")
    @ApiResponse(responseCode = "404", description = "Tariff not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public TariffResponse update(@PathVariable Long id, @Valid @RequestBody TariffRequest request) {
        return tariffService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a tariff")
    @ApiResponse(responseCode = "204", description = "Tariff deleted")
    @ApiResponse(responseCode = "404", description = "Tariff not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tariffService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
