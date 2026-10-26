package com.gens.fleetapi.controller;

import com.gens.fleetapi.dto.request.VehicleRequestDto;
import com.gens.fleetapi.dto.response.VehicleResponseDto;
import com.gens.fleetapi.infra.exception.StandardError;
import com.gens.fleetapi.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vehicles")
@Tag(name = "Vehicles", description = "Vehicle registration and lifecycle status management")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @Operation(summary = "Register a vehicle",
            description = "Creates a vehicle with status AVAILABLE. The license plate is normalized and must follow the Brazilian format.")
    @ApiResponse(responseCode = "201", description = "Vehicle created")
    @ApiResponse(responseCode = "400", description = "Invalid data",
            content = @Content(schema = @Schema(implementation = StandardError.class)))
    @ApiResponse(responseCode = "409", description = "License plate already in use",
            content = @Content(schema = @Schema(implementation = StandardError.class)))

    @PostMapping
    public ResponseEntity<VehicleResponseDto> create(@Valid @RequestBody VehicleRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.create(dto));
    }

    @Operation(summary = "Find a vehicle by id")
    @ApiResponse(responseCode = "200", description = "Vehicle found")
    @ApiResponse(responseCode = "404", description = "Vehicle not found",
            content = @Content(schema = @Schema(implementation = StandardError.class)))
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getById(id));
    }

    @Operation(summary = "List vehicles", description = "Paginated list. Use the page, size and sort parameters.")
    @GetMapping
    public ResponseEntity<Page<VehicleResponseDto>> getAllVehicles(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(vehicleService.getAll(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponseDto> update(
            @PathVariable Long id, @Valid @RequestBody VehicleRequestDto dto) {
        return ResponseEntity.ok(vehicleService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/in-transit")
    public ResponseEntity<VehicleResponseDto> markAsInTransit(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.markAsInTransit(id));
    }

    @PatchMapping("/{id}/maintenance")
    public ResponseEntity<VehicleResponseDto> markAsInMaintenance(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.markAsInMaintenance(id));
    }

    @PatchMapping("/{id}/available")
    public ResponseEntity<VehicleResponseDto> markAsAvailable(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.markAsAvailable(id));
    }
}