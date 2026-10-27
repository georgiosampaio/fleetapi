package com.gens.fleetapi.infra.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    private static final String SECRET = "dGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQ=";
    private static final String OTHER_SECRET = "b3V0cm8tc2VncmVkby1vdXRyby1zZWdyZWRvLW91dHJv";
    private static final long ONE_HOUR_MS = 3_600_000L;

    private JwtTokenService service;

    @BeforeEach
    void setUp() {
        service = new JwtTokenService(SECRET, ONE_HOUR_MS);
    }

    @Test
    @DisplayName("Should generate a valid token")
    void shouldGenerateValidToken() {
        String token = service.generateToken("admin@gens.com", "ADMIN");

        assertThat(token).isNotBlank();
        assertThat(service.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("Should extract email and role from a valid token")
    void shouldExtractEmailAndRole() {
        String token = service.generateToken("admin@gens.com", "ADMIN");

        assertThat(service.extractEmail(token)).isEqualTo("admin@gens.com");
        assertThat(service.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Should reject an expired token")
    void shouldRejectExpiredToken() {
        JwtTokenService shortLived = new JwtTokenService(SECRET, -1000L);

        String token = shortLived.generateToken("admin@gens.com", "ADMIN");

        assertThat(service.isTokenValid(token)).isFalse();
    }

    @Test
    @DisplayName("Should reject a token signed with another secret")
    void shouldRejectTokenSignedWithAnotherSecret() {
        JwtTokenService other = new JwtTokenService(OTHER_SECRET, ONE_HOUR_MS);

        String token = other.generateToken("admin@gens.com", "ADMIN");

        assertThat(service.isTokenValid(token)).isFalse();
    }

    @Test
    @DisplayName("Should reject a tampered token")
    void shouldRejectTamperedToken() {
        String token = service.generateToken("admin@gens.com", "ADMIN");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(service.isTokenValid(tampered)).isFalse();
    }

    @Test
    @DisplayName("Should reject malformed, empty and null tokens")
    void shouldRejectMalformedTokens() {
        assertThat(service.isTokenValid("not-a-token")).isFalse();
        assertThat(service.isTokenValid("")).isFalse();
        assertThat(service.isTokenValid(null)).isFalse();
    }
}