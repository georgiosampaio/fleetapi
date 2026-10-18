package com.gens.fleetapi.service;

import com.gens.fleetapi.domain.Vehicle;
import com.gens.fleetapi.domain.VehicleStatus;
import com.gens.fleetapi.dto.request.VehicleRequestDto;
import com.gens.fleetapi.dto.response.VehicleResponseDto;
import com.gens.fleetapi.infra.exception.ResourceNotFoundException;
import com.gens.fleetapi.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository vehicleRepository;

    @Transactional
    public VehicleResponseDto createVehicle(VehicleRequestDto vehicleRequestDTO) {

        if (vehicleRepository.existsByLicensePlate(vehicleRequestDTO.licensePlate())) {
            throw new IllegalArgumentException("Vehicle with license plate "
                    + vehicleRequestDTO.licensePlate() + " already exists.");
        }
        Vehicle vehicle = new Vehicle();

        vehicle.setModel(vehicleRequestDTO.model());
        vehicle.setManufactureYear(vehicleRequestDTO.manufactureYear());
        vehicle.setMileage(vehicleRequestDTO.mileage());
        vehicle.setStatus(VehicleStatus.AVAILABLE);

        return VehicleResponseDto.fromEntity(vehicleRepository.save(vehicle));
    }

    @Transactional(readOnly = true)
    public VehicleResponseDto getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Veículo",id));
        return VehicleResponseDto.fromEntity(vehicle);
    }

    @Transactional(readOnly = true)
    public Page<VehicleResponseDto> getAllVehicles(Pageable pageable) {
        return vehicleRepository.findAll(pageable).map(VehicleResponseDto::fromEntity);
    }

    @Transactional
    public VehicleResponseDto updateVehicle(Long id, VehicleRequestDto vehicleRequestDto) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + id));

        vehicle.setLicensePlate(vehicleRequestDto.licensePlate());
        vehicle.setBrand(vehicleRequestDto.brand());
        vehicle.setModel(vehicleRequestDto.model());
        vehicle.setManufactureYear(vehicleRequestDto.manufactureYear());
        vehicle.setMileage(vehicleRequestDto.mileage());

        return VehicleResponseDto.fromEntity(vehicleRepository.save(vehicle));
    }

    @Transactional
    public void deleteVehicle(Long id) {
        if (vehicleRepository.existsById(id)) {
            throw new IllegalArgumentException("Vehicle not found with ID: " + id);
        }
        vehicleRepository.deleteById(id);
    }


}