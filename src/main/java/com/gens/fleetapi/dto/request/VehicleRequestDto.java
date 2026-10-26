package com.gens.fleetapi.dto.request;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload to register or update a vehicle")
public record VehicleRequestDto(

        @Schema(description = "Brazilian license plate, old (ABC1234) or Mercosul (ABC1D23). Hyphens and spaces are ignored.", example = "ABC1D23")
        @NotBlank(message = "License plate is required")
        @Size(min = 7, max = 10, message = "License plate must be between 7 and 10 characters")
        String licensePlate,

        @Schema(description = "Vehicle brand", example = "Toyota")
        @NotBlank(message = "Brand is required")
        @Size(max = 50, message = "Brand must not exceed 50 characters")
        String brand,

        @Schema(description = "Vehicle model", example = "Hilux")
        @NotBlank(message = "Model is required")
        @Size(max = 50, message = "Model must not exceed 50 characters")
        String model,

        @Schema(description = "Manufacture year, from 1900 up to next year", example = "2022")
        @NotNull(message = "Manufacture year is required")
        @Min(value = 1900, message = "Manufacture year must be greater than or equal to 1900")
        Integer manufactureYear,

        @Schema(description = "Mileage in km. On updates it cannot be lower than the current mileage.", example = "15000.0")
        @NotNull(message = "Mileage is required")
        @PositiveOrZero(message = "Mileage must be greater than or equal to zero")
        Double mileage
) {
}
