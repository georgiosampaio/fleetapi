package com.gens.fleetapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credentials used to log in")
public record LoginRequest(

        @Schema(description = "User email", example = "admin@gens.com")
        @NotBlank(message = "Email is required")
        String email,

        @Schema(description = "User password", example = "your-password")
        @NotBlank(message = "Password is required")
        String password
) {
}