package com.gens.fleetapi.controller;

import com.gens.fleetapi.infra.security.JwtTokenService;
import com.gens.fleetapi.service.VehicleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleControllerBadRequestTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehicleService vehicleService;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @Test
    @DisplayName("Should return 400 when the id is not numeric")
    void shouldReturn400WhenIdIsNotNumeric() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter 'id' has an invalid value."));
    }

    @Test
    @DisplayName("Should return 400 when the JSON body is malformed")
    void shouldReturn400WhenJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is missing or contains invalid JSON."));
    }

    @Test
    @DisplayName("Should return 400 when sorting by a property that does not exist")
    void shouldReturn400WhenSortPropertyDoesNotExist() throws Exception {
        Mockito.when(vehicleService.getAll(ArgumentMatchers.any(Pageable.class)))
                .thenThrow(Mockito.mock(PropertyReferenceException.class));

        mockMvc.perform(get("/api/v1/vehicles").param("sort", "xyz"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid sort property."));
    }
}