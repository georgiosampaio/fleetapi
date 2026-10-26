package com.gens.fleetapi.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fleetApiOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fleet API")
                        .version("v1")
                        .description("Corporate fleet management API: vehicle registration, "
                                + "lifecycle status control and standardized error responses."));
    }
}