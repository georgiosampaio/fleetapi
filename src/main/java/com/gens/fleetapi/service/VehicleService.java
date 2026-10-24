package com.gens.fleetapi.service;

import com.gens.fleetapi.domain.Vehicle;
import com.gens.fleetapi.dto.request.VehicleRequestDto;
import com.gens.fleetapi.dto.response.VehicleResponseDto;
import com.gens.fleetapi.infra.exception.DuplicateResourceException;
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
    public VehicleResponseDto create(VehicleRequestDto requestDto) {
        String licensePlate = Vehicle.normalizeLicensePlate(requestDto.licensePlate());
        checkDuplicateLicensePlate(licensePlate);

        Vehicle vehicle = new Vehicle(
                licensePlate,
                requestDto.brand(),
                requestDto.model(),
                requestDto.manufactureYear(),
                requestDto.mileage()
        );

        return VehicleResponseDto.fromEntity(vehicleRepository.save(vehicle));
    }

    @Transactional(readOnly = true)
    public VehicleResponseDto getById(Long id) {
        return VehicleResponseDto.fromEntity(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<VehicleResponseDto> getAll(Pageable pageable) {
        return vehicleRepository.findAll(pageable).map(VehicleResponseDto::fromEntity);
    }

    @Transactional
    public VehicleResponseDto update(Long id, VehicleRequestDto requestDto) {
        Vehicle vehicle = findOrThrow(id);

        String licensePlate = Vehicle.normalizeLicensePlate(requestDto.licensePlate());
        if (!licensePlate.equals(vehicle.getLicensePlate())) {
            checkDuplicateLicensePlate(licensePlate);
        }

        vehicle.updateDetails(
                licensePlate,
                requestDto.brand(),
                requestDto.model(),
                requestDto.manufactureYear(),
                requestDto.mileage()
        );

        return VehicleResponseDto.fromEntity(vehicle);
    }

    @Transactional
    public VehicleResponseDto markAsInTransit(Long id) {
        Vehicle vehicle = findOrThrow(id);
        vehicle.markAsInTransit();
        return VehicleResponseDto.fromEntity(vehicle);
    }

    @Transactional
    public VehicleResponseDto markAsInMaintenance(Long id) {
        Vehicle vehicle = findOrThrow(id);
        vehicle.markAsInMaintenance();
        return VehicleResponseDto.fromEntity(vehicle);
    }

    @Transactional
    public VehicleResponseDto markAsAvailable(Long id) {
        Vehicle vehicle = findOrThrow(id);
        vehicle.markAsAvailable();
        return VehicleResponseDto.fromEntity(vehicle);
    }

    @Transactional
    public void delete(Long id) {
        vehicleRepository.delete(findOrThrow(id));
    }

    private Vehicle findOrThrow(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehícle", id));
    }

    private void checkDuplicateLicensePlate(String licensePlate) {
        if (vehicleRepository.existsByLicensePlate(licensePlate)) {
            throw new DuplicateResourceException("The Licence Plate " + licensePlate + " already in use.");
        }
    }
}