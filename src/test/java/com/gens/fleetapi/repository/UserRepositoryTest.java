package com.gens.fleetapi.repository;

import com.gens.fleetapi.domain.Role;
import com.gens.fleetapi.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@DisplayName("UserRepository")
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private UserRepository userRepository;

    private User user(String email, Role role) {
        return new User(email, "hashed-password", role);
    }

    @Test
    @DisplayName("should persist a user with a generated id and the chosen role")
    void shouldPersistUser() {
        User saved = userRepository.saveAndFlush(user("Ana@Gens.com", Role.ADMIN));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("ana@gens.com");
        assertThat(saved.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("existsByEmail should return true when the email exists")
    void shouldReturnTrueWhenEmailExists() {
        userRepository.saveAndFlush(user("ana@gens.com", Role.USER));

        assertThat(userRepository.existsByEmail("ana@gens.com")).isTrue();
    }

    @Test
    @DisplayName("existsByEmail should return false when the email does not exist")
    void shouldReturnFalseWhenEmailDoesNotExist() {
        assertThat(userRepository.existsByEmail("ana@gens.com")).isFalse();
    }

    @Test
    @DisplayName("findByEmail should return the user with its role when it exists")
    void shouldFindByEmail() {
        userRepository.saveAndFlush(user("ana@gens.com", Role.ADMIN));

        Optional<User> found = userRepository.findByEmail("ana@gens.com");

        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("findByEmail should return empty when it does not exist")
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        assertThat(userRepository.findByEmail("ana@gens.com")).isEmpty();
    }

    @Test
    @DisplayName("should reject a duplicated email at the database level")
    void shouldRejectDuplicatedEmail() {
        userRepository.saveAndFlush(user("ana@gens.com", Role.USER));

        assertThatThrownBy(() -> userRepository.saveAndFlush(user("ana@gens.com", Role.ADMIN)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}