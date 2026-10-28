package com.gens.fleetapi.infra.security;

import com.gens.fleetapi.controller.VehicleController;
import com.gens.fleetapi.service.VehicleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenService.class})
@TestPropertySource(properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQ=",
        "app.security.jwt.expiration-ms=3600000"
})
class VehicleSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private VehicleService vehicleService;

    private String bearer(String role) {
        return "Bearer " + jwtTokenService.generateToken(role.toLowerCase() + "@gens.com", role);
    }

    @Test
    @DisplayName("Should return 401 when no token is sent")
    void shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when the token is invalid")
    void shouldReturn401WithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should allow USER to read vehicles")
    void shouldAllowUserToRead() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles").header("Authorization", bearer("USER")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 403 when USER tries to delete a vehicle")
    void shouldForbidUserToDelete() throws Exception {
        mockMvc.perform(delete("/api/v1/vehicles/1").header("Authorization", bearer("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow ADMIN to delete a vehicle")
    void shouldAllowAdminToDelete() throws Exception {
        mockMvc.perform(delete("/api/v1/vehicles/1").header("Authorization", bearer("ADMIN")))
                .andExpect(status().isNoContent());
    }
}