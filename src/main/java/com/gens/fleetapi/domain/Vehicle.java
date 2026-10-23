package com.gens.fleetapi.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Year;
import java.util.Locale;
import java.util.regex.Pattern;

@Entity
@Table(name = "tb_vehicles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Vehicle {

    private static final int MIN_MANUFACTURE_YEAR = 1900;
    private static final int FUTURE_YEAR_TOLERANCE = 1;
    private static final int LICENSE_PLATE_LENGTH = 7;
    private static final int MAX_TEXT_LENGTH = 50;
    private static final Pattern PLATE_SEPARATORS = Pattern.compile("[\\s-]");
    private static final Pattern PLATE_FORMAT = Pattern.compile("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}");
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = LICENSE_PLATE_LENGTH)
    private String licensePlate;

    @Column(nullable = false, length = MAX_TEXT_LENGTH)
    private String brand;

    @Column(nullable = false, length = MAX_TEXT_LENGTH)
    private String model;

    @Column(nullable = false)
    private Integer manufactureYear;

    @Column(nullable = false)
    private Double mileage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status;

    public Vehicle(String licensePlate, String brand, String model, Integer manufactureYear, Double mileage) {
        applyDetails(licensePlate, brand, model, manufactureYear, mileage);
        this.status = VehicleStatus.AVAILABLE;
    }

    public void updateDetails(String licensePlate, String brand, String model, Integer manufactureYear, Double mileage) {
        applyDetails(licensePlate, brand, model, manufactureYear, mileage);
    }

    public void markAsInMaintenance() {
        if (this.status != VehicleStatus.AVAILABLE) {
            throw new IllegalStateException("Only available vehicles can be sent for maintenance. Current status:: " + this.status);
        }
        this.status = VehicleStatus.MAINTENANCE;
    }

    public void markAsInTransit() {
        if (this.status != VehicleStatus.AVAILABLE) {
            throw new IllegalStateException("Only available vehicles can enter transit. Current status: " + this.status);
        }
        this.status = VehicleStatus.IN_TRANSIT;
    }

    public void markAsAvailable() {
        if (this.status != VehicleStatus.MAINTENANCE && this.status != VehicleStatus.IN_TRANSIT) {
            throw new IllegalStateException("Invalid transition to available. Current status: " + this.status);
        }
        this.status = VehicleStatus.AVAILABLE;
    }

       public static String normalizeLicensePlate(String licensePlate) {
        if (licensePlate == null) {
            throw new IllegalArgumentException("License plate is not optional.");
        }
        String normalized = PLATE_SEPARATORS.matcher(licensePlate).replaceAll("").toUpperCase(Locale.ROOT);

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("License plate is not optional.");
        }
        if (!PLATE_FORMAT.matcher(normalized).matches()) {
            throw new IllegalArgumentException("The license plate must follow the Brazilian format (ABC1234 or ABC1D23).");
        }

        return normalized;
    }

    private void applyDetails(String licensePlate, String brand, String model, Integer manufactureYear, Double mileage) {
        String validPlate = normalizeLicensePlate(licensePlate);
        String validBrand = requireText(brand, "The brand");
        String validModel = requireText(model, "The Model");
        int validYear = requireValidYear(manufactureYear);
        double validMileage = requireValidMileage(mileage);

        this.licensePlate = validPlate;
        this.brand = validBrand;
        this.model = validModel;
        this.manufactureYear = validYear;
        this.mileage = validMileage;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "It is mandatory");
        }
        String trimmed = value.trim();
        if (trimmed.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException(fieldName + " cannot exceed " + MAX_TEXT_LENGTH + " characters.");
        }
        return trimmed;
    }

    private static int requireValidYear(Integer year) {
        int maxYear = Year.now().getValue() + FUTURE_YEAR_TOLERANCE;
        if (year == null || year < MIN_MANUFACTURE_YEAR || year > maxYear) {
            throw new IllegalArgumentException(
                    "Invalid year of manufacture. Must be between " + MIN_MANUFACTURE_YEAR + " e " + maxYear + ".");
        }
        return year;
    }

    private double requireValidMileage(Double newMileage) {
        if (newMileage == null || newMileage < 0) {
            throw new IllegalArgumentException("The mileage cannot be negative.");
        }
        if (this.mileage != null && newMileage < this.mileage) {
            throw new IllegalArgumentException("The new mileage cannot be less than the current mileage.");
        }
        return newMileage;
    }
}