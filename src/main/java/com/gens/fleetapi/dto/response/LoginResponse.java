package com.gens.fleetapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication result containing the JWT")
public record LoginResponse(

        @Schema(description = "JWT to send in the Authorization header")
        String token,

        @Schema(description = "Token type", example = "Bearer")
        String type
) {
}