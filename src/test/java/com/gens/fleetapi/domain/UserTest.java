package com.gens.fleetapi.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("User")
class UserTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create a user with valid data and a normalized email")
        void shouldCreateUserWithValidData() {
            User user = new User("  Ana@Gens.com ", "hashed-password", Role.USER);

            assertThat(user.getEmail()).isEqualTo("ana@gens.com");
            assertThat(user.getPasswordHash()).isEqualTo("hashed-password");
            assertThat(user.getRole()).isEqualTo(Role.USER);
        }
    }

    @Nested
    @DisplayName("Email")
    class Email {

        @ParameterizedTest(name = "\"{0}\" should be normalized to \"{1}\"")
        @CsvSource({
                "ana@gens.com, ana@gens.com",
                "ANA@GENS.COM, ana@gens.com",
                "' Ana@Gens.com ', ana@gens.com",
                "ana.silva+fleet@gens.com.br, ana.silva+fleet@gens.com.br"
        })
        void shouldNormalizeValidEmails(String input, String expected) {
            assertThat(User.normalizeEmail(input)).isEqualTo(expected);
        }

        @ParameterizedTest(name = "[{index}] should reject a missing email")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void shouldRejectMissingEmail(String email) {
            assertThatThrownBy(() -> User.normalizeEmail(email))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email is required");
        }

        @ParameterizedTest(name = "\"{0}\" should be rejected for an invalid format")
        @ValueSource(strings = {"ana", "ana@", "@gens.com", "ana@gens", "ana @gens.com", "ana@@gens.com"})
        void shouldRejectInvalidFormat(String email) {
            assertThatThrownBy(() -> User.normalizeEmail(email))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("format is invalid");
        }

        @Test
        @DisplayName("should reject an email longer than 100 characters")
        void shouldRejectEmailLongerThanLimit() {
            String tooLong = "a".repeat(95) + "@gens.com";

            assertThatThrownBy(() -> User.normalizeEmail(tooLong))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot exceed");
        }
    }

    @Nested
    @DisplayName("Password hash")
    class PasswordHash {

        @ParameterizedTest(name = "[{index}] should reject a missing password hash")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void shouldRejectMissingPasswordHash(String passwordHash) {
            assertThatThrownBy(() -> new User("ana@gens.com", passwordHash, Role.USER))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Password hash is required");
        }
    }

    @Nested
    @DisplayName("Role")
    class RoleRules {

        @Test
        @DisplayName("should reject a null role")
        void shouldRejectNullRole() {
            assertThatThrownBy(() -> new User("ana@gens.com", "hashed-password", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Role is required");
        }
    }
}