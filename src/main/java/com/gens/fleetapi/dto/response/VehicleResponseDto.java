package com.gens.fleetapi.dto.response;

import com.gens.fleetapi.domain.Vehicle;
import com.gens.fleetapi.domain.VehicleStatus;

public record VehicleResponseDto (
        Long id,
        String licensePlate,
        String brand,
        String model,
        Integer manufactureYear,
        Double mileage,
        VehicleStatus status
){
    public static VehicleResponseDto fromEntity(Vehicle vehicle) {
        return new VehicleResponseDto(
                vehicle.getId(),
                vehicle.getLicensePlate(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getManufactureYear(),
                vehicle.getMileage(),
                vehicle.getStatus()
        );
    }
}
