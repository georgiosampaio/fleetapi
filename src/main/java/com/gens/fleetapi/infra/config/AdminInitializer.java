package com.gens.fleetapi.infra.config;

import com.gens.fleetapi.domain.Role;
import com.gens.fleetapi.domain.User;
import com.gens.fleetapi.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AdminInitializer implements ApplicationRunner {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminInitializer(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            @Value("${ADMIN_EMAIL:}") String adminEmail,
                            @Value("${ADMIN_PASSWORD:}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }
        if (adminEmail.isBlank() || adminPassword.length() < MIN_PASSWORD_LENGTH) {
            log.warn("No ADMIN exists and ADMIN_EMAIL/ADMIN_PASSWORD are missing or too short. Skipping admin creation.");
            return;
        }
        userRepository.save(new User(adminEmail, passwordEncoder.encode(adminPassword), Role.ADMIN));
        log.info("Initial ADMIN user created.");
    }
}