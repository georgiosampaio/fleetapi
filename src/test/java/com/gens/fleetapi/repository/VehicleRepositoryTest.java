package com.gens.fleetapi.repository;

import com.gens.fleetapi.domain.Vehicle;
import com.gens.fleetapi.domain.VehicleStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@DisplayName("VehicleRepository")
class VehicleRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private VehicleRepository vehicleRepository;

    private Vehicle vehicle(String plate) {
        return new Vehicle(plate, "Toyota", "Hilux", 2022, 15000.0);
    }

    @Test
    @DisplayName("should persist a vehicle with a generated id and status AVAILABLE")
    void shouldPersistVehicle() {
        Vehicle saved = vehicleRepository.saveAndFlush(vehicle("ABC1234"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    @Test
    @DisplayName("existsByLicensePlate should return true when the plate exists")
    void shouldReturnTrueWhenPlateExists() {
        vehicleRepository.saveAndFlush(vehicle("ABC1234"));

        assertThat(vehicleRepository.existsByLicensePlate("ABC1234")).isTrue();
    }

    @Test
    @DisplayName("existsByLicensePlate should return false when the plate does not exist")
    void shouldReturnFalseWhenPlateDoesNotExist() {
        assertThat(vehicleRepository.existsByLicensePlate("ABC1234")).isFalse();
    }

    @Test
    @DisplayName("findByLicensePlate should return the vehicle when it exists")
    void shouldFindByLicensePlate() {
        vehicleRepository.saveAndFlush(vehicle("ABC1234"));

        Optional<Vehicle> found = vehicleRepository.findByLicensePlate("ABC1234");

        assertThat(found).isPresent();
        assertThat(found.get().getModel()).isEqualTo("Hilux");
    }

    @Test
    @DisplayName("findByLicensePlate should return empty when it does not exist")
    void shouldReturnEmptyWhenPlateDoesNotExist() {
        assertThat(vehicleRepository.findByLicensePlate("ABC1234")).isEmpty();
    }

    @Test
    @DisplayName("findByStatus should return only the vehicles with that status")
    void shouldFindByStatus() {
        vehicleRepository.save(vehicle("ABC1234"));

        Vehicle inTransit = vehicle("DEF5G67");
        inTransit.markAsInTransit();
        vehicleRepository.save(inTransit);

        Vehicle inMaintenance = vehicle("GHI8J90");
        inMaintenance.markAsInMaintenance();
        vehicleRepository.saveAndFlush(inMaintenance);

        List<Vehicle> result = vehicleRepository.findByStatus(VehicleStatus.IN_TRANSIT);

        assertThat(result).extracting(Vehicle::getLicensePlate).containsExactly("DEF5G67");
    }

    @Test
    @DisplayName("should reject a duplicated plate at the database level")
    void shouldRejectDuplicatedPlate() {
        vehicleRepository.saveAndFlush(vehicle("ABC1234"));

        assertThatThrownBy(() -> vehicleRepository.saveAndFlush(vehicle("ABC1234")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}