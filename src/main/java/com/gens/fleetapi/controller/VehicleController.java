package com.gens.fleetapi.controller;

import com.gens.fleetapi.dto.request.VehicleRequestDto;
import com.gens.fleetapi.dto.response.VehicleResponseDto;
import com.gens.fleetapi.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<VehicleResponseDto> create(@Valid @RequestBody VehicleRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<VehicleResponseDto>> getAllVehicles(Pageable pageable) {
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