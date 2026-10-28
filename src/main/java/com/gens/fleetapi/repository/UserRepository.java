package com.gens.fleetapi.repository;

import com.gens.fleetapi.domain.Role;
import com.gens.fleetapi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByRole(Role role);
    Optional<User> findByEmail(String email);
}
