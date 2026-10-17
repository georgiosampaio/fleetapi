package com.gens.fleetapi.repository;

import com.gens.fleetapi.domain.Vehicle;
import com.gens.fleetapi.domain.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    boolean existsByLicensePlate(String licencePlate);
    Optional<Vehicle> findByLicensePlate(String licencePlate);
    List<Vehicle> findByStatus(VehicleStatus status);
}
