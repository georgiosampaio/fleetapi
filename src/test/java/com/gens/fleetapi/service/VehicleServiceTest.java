package com.gens.fleetapi.service;

import com.gens.fleetapi.domain.Vehicle;
import com.gens.fleetapi.domain.VehicleStatus;
import com.gens.fleetapi.dto.request.VehicleRequestDto;
import com.gens.fleetapi.dto.response.VehicleResponseDto;
import com.gens.fleetapi.infra.exception.DuplicateResourceException;
import com.gens.fleetapi.repository.VehicleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.gens.fleetapi.infra.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleService")
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private VehicleRequestDto validRequest() {
        return new VehicleRequestDto("abc-1234", "Toyota", "Hilux", 2022, 15000.0);
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should save and return a vehicle with normalized plate")
        void shouldCreateVehicle() {
            when(vehicleRepository.existsByLicensePlate("ABC1234")).thenReturn(false);
            when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

            VehicleResponseDto response = vehicleService.create(validRequest());

            assertThat(response.licensePlate()).isEqualTo("ABC1234");
            assertThat(response.brand()).isEqualTo("Toyota");
            assertThat(response.status()).isEqualTo(VehicleStatus.AVAILABLE);
            verify(vehicleRepository).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("should reject a duplicated plate and never save")
        void shouldRejectDuplicatedPlate() {
            when(vehicleRepository.existsByLicensePlate("ABC1234")).thenReturn(true);

            assertThatThrownBy(() -> vehicleService.create(validRequest()))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("already in use");

            verify(vehicleRepository, never()).save(any());
        }

        @Test
        @DisplayName("should reject a plate outside the Brazilian format without touching the database")
        void shouldRejectInvalidPlate() {
            VehicleRequestDto request = new VehicleRequestDto("INVALID", "Toyota", "Hilux", 2022, 15000.0);

            assertThatThrownBy(() -> vehicleService.create(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Brazilian format");

            verifyNoInteractions(vehicleRepository);
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return the vehicle when it exists")
        void shouldReturnVehicle() {
            Vehicle vehicle = new Vehicle("ABC1234", "Toyota", "Hilux", 2022, 15000.0);
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

            VehicleResponseDto response = vehicleService.getById(1L);

            assertThat(response.licensePlate()).isEqualTo("ABC1234");
            assertThat(response.model()).isEqualTo("Hilux");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when it does not exist")
        void shouldThrowWhenNotFound() {
            when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vehicleService.getById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("not found with ID: 99");
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("should return a page with the vehicles converted to DTOs")
        void shouldReturnPageOfDtos() {
            Pageable pageable = PageRequest.of(0, 10);
            Vehicle first = new Vehicle("ABC1234", "Toyota", "Hilux", 2022, 15000.0);
            Vehicle second = new Vehicle("DEF5G67", "Ford", "Ranger", 2023, 5000.0);
            when(vehicleRepository.findAll(pageable))
                    .thenReturn(new PageImpl<>(List.of(first, second), pageable, 2));

            Page<VehicleResponseDto> result = vehicleService.getAll(pageable);

            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).licensePlate()).isEqualTo("ABC1234");
            assertThat(result.getContent().get(1).licensePlate()).isEqualTo("DEF5G67");
        }

        @Test
        @DisplayName("should return an empty page when there are no vehicles")
        void shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(vehicleRepository.findAll(pageable))
                    .thenReturn(new PageImpl<>(List.of(), pageable, 0));

            Page<VehicleResponseDto> result = vehicleService.getAll(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete the vehicle when it exists")
        void shouldDeleteVehicle() {
            Vehicle vehicle = new Vehicle("ABC1234", "Toyota", "Hilux", 2022, 15000.0);
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

            vehicleService.delete(1L);

            verify(vehicleRepository).delete(vehicle);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException and never delete when it does not exist")
        void shouldThrowWhenDeletingMissingVehicle() {
            when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vehicleService.delete(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(vehicleRepository, never()).delete(any(Vehicle.class));
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should update the vehicle when the new plate is free")
        void shouldUpdateVehicle() {
            Vehicle vehicle = new Vehicle("ABC1234", "Toyota", "Hilux", 2022, 15000.0);
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
            when(vehicleRepository.existsByLicensePlate("DEF5G67")).thenReturn(false);
            VehicleRequestDto request = new VehicleRequestDto("DEF5G67", "Ford", "Ranger", 2023, 20000.0);

            VehicleResponseDto response = vehicleService.update(1L, request);

            assertThat(response.licensePlate()).isEqualTo("DEF5G67");
            assertThat(response.brand()).isEqualTo("Ford");
            assertThat(response.mileage()).isEqualTo(20000.0);
        }

        @Test
        @DisplayName("should not check duplicates when the plate does not change")
        void shouldSkipDuplicateCheckWhenPlateUnchanged() {
            Vehicle vehicle = new Vehicle("ABC1234", "Toyota", "Hilux", 2022, 15000.0);
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
            VehicleRequestDto request = new VehicleRequestDto("abc-1234", "Toyota", "Hilux", 2022, 16000.0);

            VehicleResponseDto response = vehicleService.update(1L, request);

            assertThat(response.mileage()).isEqualTo(16000.0);
            verify(vehicleRepository, never()).existsByLicensePlate(any(String.class));
        }

        @Test
        @DisplayName("should reject a plate already used by another vehicle and keep the current data")
        void shouldRejectDuplicatedPlate() {
            Vehicle vehicle = new Vehicle("ABC1234", "Toyota", "Hilux", 2022, 15000.0);
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
            when(vehicleRepository.existsByLicensePlate("DEF5G67")).thenReturn(true);
            VehicleRequestDto request = new VehicleRequestDto("DEF5G67", "Ford", "Ranger", 2023, 20000.0);

            assertThatThrownBy(() -> vehicleService.update(1L, request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("already in use");

            assertThat(vehicle.getLicensePlate()).isEqualTo("ABC1234");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when the vehicle does not exist")
        void shouldThrowWhenNotFound() {
            when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vehicleService.update(99L, validRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("status transitions")
    class StatusTransitions {

        @Test
        @DisplayName("markAsInTransit should move an available vehicle to IN_TRANSIT")
        void shouldMarkAsInTransit() {
            Vehicle vehicle = new Vehicle("ABC1234", "Toyota", "Hilux", 2022, 15000.0);
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

            VehicleResponseDto response = vehicleService.markAsInTransit(1L);

            assertThat(response.status()).isEqualTo(VehicleStatus.IN_TRANSIT);
        }

        @Test
        @DisplayName("markAsInMaintenance should move an available vehicle to MAINTENANCE")
        void shouldMarkAsInMaintenance() {
            Vehicle vehicle = new Vehicle("ABC1234", "Toyota", "Hilux", 2022, 15000.0);
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

            VehicleResponseDto response = vehicleService.markAsInMaintenance(1L);

            assertThat(response.status()).isEqualTo(VehicleStatus.MAINTENANCE);
        }

        @Test
        @DisplayName("markAsAvailable should bring a vehicle in maintenance back to AVAILABLE")
        void shouldMarkAsAvailable() {
            Vehicle vehicle = new Vehicle("ABC1234", "Toyota", "Hilux", 2022, 15000.0);
            vehicle.markAsInMaintenance();
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

            VehicleResponseDto response = vehicleService.markAsAvailable(1L);

            assertThat(response.status()).isEqualTo(VehicleStatus.AVAILABLE);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when the vehicle does not exist")
        void shouldThrowWhenNotFound() {
            when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vehicleService.markAsInTransit(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should let the entity reject a forbidden transition")
        void shouldPropagateInvalidTransition() {
            Vehicle vehicle = new Vehicle("ABC1234", "Toyota", "Hilux", 2022, 15000.0);
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

            assertThatThrownBy(() -> vehicleService.markAsAvailable(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid transition to available");
        }
    }

}