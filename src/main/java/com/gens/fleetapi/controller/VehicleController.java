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
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.createVehicle(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @GetMapping
    public ResponseEntity<Page<VehicleResponseDto>> getAllVehicles(Pageable pageable) {
        return ResponseEntity.ok(vehicleService.getAllVehicles(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponseDto> update(
            @PathVariable Long id, @Valid @RequestBody VehicleRequestDto dto){
        return ResponseEntity.ok(vehicleService.updateVehicle(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }


}