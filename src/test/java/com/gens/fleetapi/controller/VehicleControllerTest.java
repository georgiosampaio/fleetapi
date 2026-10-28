package com.gens.fleetapi.controller;

import com.gens.fleetapi.domain.VehicleStatus;
import com.gens.fleetapi.dto.request.VehicleRequestDto;
import com.gens.fleetapi.dto.response.VehicleResponseDto;
import com.gens.fleetapi.infra.exception.DuplicateResourceException;
import com.gens.fleetapi.infra.security.JwtTokenService;
import com.gens.fleetapi.service.VehicleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gens.fleetapi.infra.exception.ResourceNotFoundException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@WebMvcTest(VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("VehicleController")
class VehicleControllerTest {

    private static final String BASE_URL = "/api/v1/vehicles";

    private static final String VALID_JSON = """
            {
              "licensePlate": "ABC1234",
              "brand": "Toyota",
              "model": "Hilux",
              "manufactureYear": 2022,
              "mileage": 15000.0
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehicleService vehicleService;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @Nested
    @DisplayName("POST /api/v1/vehicles")
    class Create {

        @Test
        @DisplayName("should return 201 and the created vehicle")
        void shouldReturnCreated() throws Exception {
            VehicleResponseDto response = new VehicleResponseDto(
                    1L, "ABC1234", "Toyota", "Hilux", 2022, 15000.0, VehicleStatus.AVAILABLE);
            when(vehicleService.create(any(VehicleRequestDto.class))).thenReturn(response);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.licensePlate").value("ABC1234"))
                    .andExpect(jsonPath("$.status").value("AVAILABLE"));
        }

        @Test
        @DisplayName("should return 400 with the validation message and never call the service")
        void shouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
            String invalidJson = """
                    {
                      "licensePlate": "ABC1234",
                      "brand": "",
                      "model": "Hilux",
                      "manufactureYear": 2022,
                      "mileage": 15000.0
                    }
                    """;

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation error"))
                    .andExpect(jsonPath("$.message").value(containsString("Brand is required")))
                    .andExpect(jsonPath("$.path").value(BASE_URL));

            verifyNoInteractions(vehicleService);
        }

        @Test
        @DisplayName("should return 409 when the plate is already in use")
        void shouldReturnConflictWhenPlateIsDuplicated() throws Exception {
            when(vehicleService.create(any(VehicleRequestDto.class)))
                    .thenThrow(new DuplicateResourceException("The Licence Plate ABC1234 already in use."));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Duplicate Feature"))
                    .andExpect(jsonPath("$.message").value(containsString("already in use")));
        }

        @Test
        @DisplayName("should return 400 when the entity rejects the data (invariant violation)")
        void shouldReturnBadRequestWhenInvariantIsViolated() throws Exception {
            when(vehicleService.create(any(VehicleRequestDto.class)))
                    .thenThrow(new IllegalArgumentException("The license plate must follow the Brazilian format (ABC1234 or ABC1D23)."));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Invalid request (Invariant violation)"))
                    .andExpect(jsonPath("$.message").value(containsString("Brazilian format")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vehicles/{id}")
    class FindById {

        @Test
        @DisplayName("should return 200 and the vehicle")
        void shouldReturnVehicle() throws Exception {
            VehicleResponseDto response = new VehicleResponseDto(
                    1L, "ABC1234", "Toyota", "Hilux", 2022, 15000.0, VehicleStatus.AVAILABLE);
            when(vehicleService.getById(1L)).thenReturn(response);

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.licensePlate").value("ABC1234"))
                    .andExpect(jsonPath("$.model").value("Hilux"));
        }

        @Test
        @DisplayName("should return 404 with the error JSON when the vehicle does not exist")
        void shouldReturnNotFound() throws Exception {
            when(vehicleService.getById(99L)).thenThrow(new ResourceNotFoundException("Vehicle", 99L));

            mockMvc.perform(get(BASE_URL + "/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Resource not found"))
                    .andExpect(jsonPath("$.message").value(containsString("not found with ID: 99")))
                    .andExpect(jsonPath("$.path").value(BASE_URL + "/99"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vehicles")
    class GetAll {

        @Test
        @DisplayName("should return 200 and a page with the vehicles")
        void shouldReturnPage() throws Exception {
            VehicleResponseDto first = new VehicleResponseDto(
                    1L, "ABC1234", "Toyota", "Hilux", 2022, 15000.0, VehicleStatus.AVAILABLE);
            VehicleResponseDto second = new VehicleResponseDto(
                    2L, "DEF5G67", "Ford", "Ranger", 2023, 5000.0, VehicleStatus.AVAILABLE);
            when(vehicleService.getAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(first, second)));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].licensePlate").value("ABC1234"))
                    .andExpect(jsonPath("$.content[1].licensePlate").value("DEF5G67"));
        }

        @Test
        @DisplayName("should return 200 and an empty page when there are no vehicles")
        void shouldReturnEmptyPage() throws Exception {
            when(vehicleService.getAll(any(Pageable.class))).thenReturn(new PageImpl<VehicleResponseDto>(List.of()));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @DisplayName("should forward the page and size parameters to the service")
        void shouldForwardPaginationParameters() throws Exception {
            when(vehicleService.getAll(any(Pageable.class))).thenReturn(new PageImpl<VehicleResponseDto>(List.of()));

            mockMvc.perform(get(BASE_URL).param("page", "1").param("size", "5"))
                    .andExpect(status().isOk());

            verify(vehicleService).getAll(argThat((Pageable p) -> p.getPageNumber() == 1 && p.getPageSize() == 5));
        }

        @Nested
        @DisplayName("PUT /api/v1/vehicles/{id}")
        class Update {

            @Test
            @DisplayName("should return 200 and the updated vehicle")
            void shouldReturnUpdatedVehicle() throws Exception {
                VehicleResponseDto response = new VehicleResponseDto(
                        1L, "ABC1234", "Toyota", "Hilux", 2022, 15000.0, VehicleStatus.AVAILABLE);
                when(vehicleService.update(eq(1L), any(VehicleRequestDto.class))).thenReturn(response);

                mockMvc.perform(put(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(1))
                        .andExpect(jsonPath("$.licensePlate").value("ABC1234"));
            }

            @Test
            @DisplayName("should return 404 when the vehicle does not exist")
            void shouldReturnNotFound() throws Exception {
                when(vehicleService.update(eq(99L), any(VehicleRequestDto.class)))
                        .thenThrow(new ResourceNotFoundException("Vehicle", 99L));

                mockMvc.perform(put(BASE_URL + "/99")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_JSON))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.error").value("Resource not found"))
                        .andExpect(jsonPath("$.path").value(BASE_URL + "/99"));
            }

            @Test
            @DisplayName("should return 400 for a negative mileage and never call the service")
            void shouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
                String invalidJson = """
                    {
                      "licensePlate": "ABC1234",
                      "brand": "Toyota",
                      "model": "Hilux",
                      "manufactureYear": 2022,
                      "mileage": -5.0
                    }
                    """;

                mockMvc.perform(put(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.error").value("Validation error"))
                        .andExpect(jsonPath("$.message").value(containsString("Mileage must be greater than or equal to zero")));

                verifyNoInteractions(vehicleService);
            }
        }

        @Nested
        @DisplayName("DELETE /api/v1/vehicles/{id}")
        class Delete {

            @Test
            @DisplayName("should return 204 with no body and call the service")
            void shouldReturnNoContent() throws Exception {
                mockMvc.perform(delete(BASE_URL + "/1"))
                        .andExpect(status().isNoContent());

                verify(vehicleService).delete(1L);
            }

            @Test
            @DisplayName("should return 404 when the vehicle does not exist")
            void shouldReturnNotFound() throws Exception {
                doThrow(new ResourceNotFoundException("Vehicle", 99L)).when(vehicleService).delete(99L);

                mockMvc.perform(delete(BASE_URL + "/99"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value(containsString("not found with ID: 99")));
            }
        }

        @Nested
        @DisplayName("PATCH /api/v1/vehicles/{id}/...")
        class StatusTransitions {

            private VehicleResponseDto vehicleWithStatus(VehicleStatus status) {
                return new VehicleResponseDto(1L, "ABC1234", "Toyota", "Hilux", 2022, 15000.0, status);
            }

            @Test
            @DisplayName("in-transit should return 200 with status IN_TRANSIT")
            void shouldMarkAsInTransit() throws Exception {
                when(vehicleService.markAsInTransit(1L)).thenReturn(vehicleWithStatus(VehicleStatus.IN_TRANSIT));

                mockMvc.perform(patch(BASE_URL + "/1/in-transit"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value("IN_TRANSIT"));
            }

            @Test
            @DisplayName("maintenance should return 200 with status MAINTENANCE")
            void shouldMarkAsInMaintenance() throws Exception {
                when(vehicleService.markAsInMaintenance(1L)).thenReturn(vehicleWithStatus(VehicleStatus.MAINTENANCE));

                mockMvc.perform(patch(BASE_URL + "/1/maintenance"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value("MAINTENANCE"));
            }

            @Test
            @DisplayName("available should return 200 with status AVAILABLE")
            void shouldMarkAsAvailable() throws Exception {
                when(vehicleService.markAsAvailable(1L)).thenReturn(vehicleWithStatus(VehicleStatus.AVAILABLE));

                mockMvc.perform(patch(BASE_URL + "/1/available"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value("AVAILABLE"));
            }

            @Test
            @DisplayName("should return 409 when the transition is forbidden")
            void shouldReturnConflictWhenTransitionIsForbidden() throws Exception {
                when(vehicleService.markAsInTransit(1L))
                        .thenThrow(new IllegalStateException("Only available vehicles can enter transit. Current status: MAINTENANCE"));

                mockMvc.perform(patch(BASE_URL + "/1/in-transit"))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.status").value(409))
                        .andExpect(jsonPath("$.error").value("Business State Conflict"))
                        .andExpect(jsonPath("$.message").value(containsString("Only available vehicles can enter transit")));
            }

            @Test
            @DisplayName("should return 404 when the vehicle does not exist")
            void shouldReturnNotFound() throws Exception {
                when(vehicleService.markAsAvailable(99L)).thenThrow(new ResourceNotFoundException("Vehicle", 99L));

                mockMvc.perform(patch(BASE_URL + "/99/available"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.error").value("Resource not found"));
            }
        }
    }
















}