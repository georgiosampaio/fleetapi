package com.gens.fleetapi.infra.security;

import com.gens.fleetapi.domain.User;
import com.gens.fleetapi.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(User.normalizeEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
        return new AuthenticatedUser(user);
    }
}