package com.gens.fleetapi.dto.request;

import jakarta.validation.constraints.*;

public record VehicleRequestDto(

        @NotBlank(message = "License plate is required")
        @Size(min = 7, max = 10, message = "License plate must be between 7 and 10 characters")
        String licensePlate,

        @NotBlank(message = "Brand is required")
        @Size(max = 50, message = "Brand must not exceed 50 characters")
        String brand,

        @NotBlank(message = "Model is required")
        @Size(max = 50, message = "Model must not exceed 50 characters")
        String model,

        @NotNull(message = "Manufacture year is required")
        @Min(value = 1900, message = "Manufacture year must be greater than or equal to 1900")
        Integer manufactureYear,

        @NotNull(message = "Mileage is required")
        @PositiveOrZero(message = "Mileage must be greater than or equal to zero")
        Double mileage
) {
}
