package com.gens.fleetapi.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Vehicle")
class VehicleTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create a vehicle with valid data and start as AVAILABLE")
        void shouldCreateVehicleWithValidData() {
            Vehicle vehicle = new Vehicle("abc-1234", "  Toyota ", "Hilux", 2022, 15000.0);

            assertThat(vehicle.getLicensePlate()).isEqualTo("ABC1234");
            assertThat(vehicle.getBrand()).isEqualTo("Toyota");
            assertThat(vehicle.getModel()).isEqualTo("Hilux");
            assertThat(vehicle.getManufactureYear()).isEqualTo(2022);
            assertThat(vehicle.getMileage()).isEqualTo(15000.0);
            assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        }
    }

    @Nested
    @DisplayName("License plate")
    class LicensePlate {

        @ParameterizedTest(name = "\"{0}\" should be normalized to \"{1}\"")
        @CsvSource({
                "abc1234, ABC1234",
                "ABC-1234, ABC1234",
                "abc 1234, ABC1234",
                "abc1d23, ABC1D23",
                "' abc-1d23 ', ABC1D23"
        })
        void shouldNormalizeValidPlates(String input, String expected) {
            assertThat(Vehicle.normalizeLicensePlate(input)).isEqualTo(expected);
        }

        @ParameterizedTest(name = "[{index}] should reject missing plate")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "---", " - "})
        void shouldRejectMissingPlate(String plate) {
            assertThatThrownBy(() -> Vehicle.normalizeLicensePlate(plate))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not optional");
        }

        @ParameterizedTest(name = "\"{0}\" should be rejected for not following the Brazilian format")
        @ValueSource(strings = {
                "ABC123",
                "ABC12345",
                "AB12345",
                "1234ABC",
                "ABCD123",
                "ABC1D2E",
                "ABC.1234",
                "ABÇ1234"
        })

        void shouldRejectPlatesOutsideBrazilianFormat(String plate) {
            assertThatThrownBy(() -> Vehicle.normalizeLicensePlate(plate))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Brazilian format");
        }
    }

    @Nested
    @DisplayName("Manufacture year")
    class ManufactureYear {

        private Vehicle createWithYear(Integer year) {
            return new Vehicle("ABC1234", "Toyota", "Hilux", year, 0.0);
        }

        @Test
        @DisplayName("should accept the minimum year (1900)")
        void shouldAcceptMinimumYear() {
            assertThat(createWithYear(1900).getManufactureYear()).isEqualTo(1900);
        }

        @Test
        @DisplayName("should accept next year (current year + 1)")
        void shouldAcceptNextYear() {
            int nextYear = Year.now().getValue() + 1;

            assertThat(createWithYear(nextYear).getManufactureYear()).isEqualTo(nextYear);
        }

        @Test
        @DisplayName("should reject a year below the minimum (1899)")
        void shouldRejectYearBelowMinimum() {
            assertThatThrownBy(() -> createWithYear(1899))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid year");
        }

        @Test
        @DisplayName("should reject a year beyond next year (current year + 2)")
        void shouldRejectYearAboveMaximum() {
            int tooFarYear = Year.now().getValue() + 2;

            assertThatThrownBy(() -> createWithYear(tooFarYear))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid year");
        }

        @Test
        @DisplayName("should reject a null year")
        void shouldRejectNullYear() {
            assertThatThrownBy(() -> createWithYear(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid year");
        }
    }

    @Nested
    @DisplayName("Mileage")
    class Mileage {

        private Vehicle vehicleWithMileage(Double mileage) {
            return new Vehicle("ABC1234", "Toyota", "Hilux", 2022, mileage);
        }

        @Test
        @DisplayName("should accept zero mileage")
        void shouldAcceptZeroMileage() {
            assertThat(vehicleWithMileage(0.0).getMileage()).isEqualTo(0.0);
        }

        @ParameterizedTest(name = "{0} should be rejected as negative")
        @ValueSource(doubles = {-0.1, -1.0, -1000.0})
        void shouldRejectNegativeMileage(double mileage) {
            assertThatThrownBy(() -> vehicleWithMileage(mileage))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be negative");
        }

        @Test
        @DisplayName("should reject null mileage")
        void shouldRejectNullMileage() {
            assertThatThrownBy(() -> vehicleWithMileage(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be negative");
        }

        @Test
        @DisplayName("should accept an update with equal or higher mileage")
        void shouldAcceptEqualOrHigherMileageOnUpdate() {
            Vehicle vehicle = vehicleWithMileage(1000.0);

            vehicle.updateDetails("ABC1234", "Toyota", "Hilux", 2022, 1000.0);
            assertThat(vehicle.getMileage()).isEqualTo(1000.0);

            vehicle.updateDetails("ABC1234", "Toyota", "Hilux", 2022, 1500.0);
            assertThat(vehicle.getMileage()).isEqualTo(1500.0);
        }

        @Test
        @DisplayName("should reject an update that lowers the mileage and keep the current value")
        void shouldRejectLowerMileageOnUpdate() {
            Vehicle vehicle = vehicleWithMileage(1000.0);

            assertThatThrownBy(() -> vehicle.updateDetails("ABC1234", "Toyota", "Hilux", 2022, 999.0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("less than the current mileage");

            assertThat(vehicle.getMileage()).isEqualTo(1000.0);
        }

        @Nested
        @DisplayName("Status transitions")
        class StatusTransitions {

            private Vehicle available() {
                return new Vehicle("ABC1234", "Toyota", "Hilux", 2022, 0.0);
            }

            private Vehicle inMaintenance() {
                Vehicle vehicle = available();
                vehicle.markAsInMaintenance();
                return vehicle;
            }

            private Vehicle inTransit() {
                Vehicle vehicle = available();
                vehicle.markAsInTransit();
                return vehicle;
            }

            @Test
            @DisplayName("AVAILABLE -> IN_TRANSIT is allowed")
            void availableToInTransit() {
                Vehicle vehicle = available();
                vehicle.markAsInTransit();
                assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.IN_TRANSIT);
            }

            @Test
            @DisplayName("AVAILABLE -> MAINTENANCE is allowed")
            void availableToMaintenance() {
                Vehicle vehicle = available();
                vehicle.markAsInMaintenance();
                assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.MAINTENANCE);
            }

            @Test
            @DisplayName("MAINTENANCE -> AVAILABLE is allowed")
            void maintenanceToAvailable() {
                Vehicle vehicle = inMaintenance();
                vehicle.markAsAvailable();
                assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
            }

            @Test
            @DisplayName("IN_TRANSIT -> AVAILABLE is allowed")
            void inTransitToAvailable() {
                Vehicle vehicle = inTransit();
                vehicle.markAsAvailable();
                assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
            }

            @Test
            @DisplayName("AVAILABLE -> AVAILABLE is rejected")
            void availableToAvailableIsRejected() {
                Vehicle vehicle = available();
                assertThatThrownBy(vehicle::markAsAvailable)
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("Invalid transition to available");
                assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
            }

            @Test
            @DisplayName("IN_TRANSIT -> IN_TRANSIT is rejected")
            void inTransitToInTransitIsRejected() {
                Vehicle vehicle = inTransit();
                assertThatThrownBy(vehicle::markAsInTransit)
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("Only available vehicles can enter transit");
                assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.IN_TRANSIT);
            }

            @Test
            @DisplayName("IN_TRANSIT -> MAINTENANCE is rejected")
            void inTransitToMaintenanceIsRejected() {
                Vehicle vehicle = inTransit();
                assertThatThrownBy(vehicle::markAsInMaintenance)
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("Only available vehicles can be sent for maintenance");
                assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.IN_TRANSIT);
            }

            @Test
            @DisplayName("MAINTENANCE -> MAINTENANCE is rejected")
            void maintenanceToMaintenanceIsRejected() {
                Vehicle vehicle = inMaintenance();
                assertThatThrownBy(vehicle::markAsInMaintenance)
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("Only available vehicles can be sent for maintenance");
                assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.MAINTENANCE);
            }

            @Test
            @DisplayName("MAINTENANCE -> IN_TRANSIT is rejected")
            void maintenanceToInTransitIsRejected() {
                Vehicle vehicle = inMaintenance();
                assertThatThrownBy(vehicle::markAsInTransit)
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("Only available vehicles can enter transit");
                assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.MAINTENANCE);
            }
        }
    }



















}