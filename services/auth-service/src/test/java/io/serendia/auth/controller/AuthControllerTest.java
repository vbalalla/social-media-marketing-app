package io.serendia.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.serendia.auth.config.JwtProperties;
import io.serendia.auth.domain.UserEntity;
import io.serendia.auth.domain.UserRole;
import io.serendia.auth.dto.AuthResponse;
import io.serendia.auth.dto.LoginRequest;
import io.serendia.auth.dto.RegisterRequest;
import io.serendia.auth.service.AuthService;
import io.serendia.auth.service.AuthService.AuthServiceTokenPair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice test — tests AuthController in isolation.
 * AuthService, JwtProperties, and security filter are mocked.
 */
@WebMvcTest(controllers = AuthController.class)
@TestPropertySource(properties = {
        "jwt.private-key=placeholder",
        "jwt.public-key=placeholder",
        "jwt.issuer=https://test.serendia.io",
        "jwt.access-token-expiry-minutes=15",
        "jwt.refresh-token-expiry-days=7"
})
@DisplayName("AuthController — Web Layer Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtProperties jwtProps;

    @MockBean
    private io.serendia.auth.security.JwtAuthenticationFilter jwtFilter;

    // -------------------------------------------------------------------------
    // POST /auth/register
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /auth/register — valid payload → 201 Created")
    void register_validRequest_returns201() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "alice@example.com", "SecurePassword1!", "Alice"
        );
        when(authService.register(any())).thenReturn(UserEntity.builder()
                .id(UUID.randomUUID()).email("alice@example.com").build());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /auth/register — missing email → 400 with fieldErrors")
    void register_missingEmail_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest("", "SecurePassword1!", "Alice");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @DisplayName("POST /auth/register — weak password → 400")
    void register_weakPassword_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest("alice@example.com", "weakpass", "Alice");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register — duplicate email → 409 Conflict")
    void register_duplicateEmail_returns409() throws Exception {
        when(authService.register(any())).thenThrow(
                new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists"));

        RegisterRequest req = new RegisterRequest(
                "dup@example.com", "SecurePassword1!", "Dup User"
        );
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    // -------------------------------------------------------------------------
    // POST /auth/login
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /auth/login — valid credentials → 200 with accessToken")
    void login_validCredentials_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        AuthResponse response = AuthResponse.of(
                "mock.jwt.token", 900L, userId, "alice@example.com", "Alice", UserRole.ADMIN
        );
        AuthServiceTokenPair pair = new AuthServiceTokenPair(response, "rawRefreshToken", Duration.ofDays(7));
        when(authService.login(any())).thenReturn(pair);
        when(jwtProps.getRefreshTokenExpiryDays()).thenReturn(7L);

        LoginRequest req = new LoginRequest("alice@example.com", "SecurePassword1!");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(cookie().exists("serendia_rt"))
                .andExpect(cookie().httpOnly("serendia_rt", true));
    }

    @Test
    @DisplayName("POST /auth/login — bad credentials → 401")
    void login_badCredentials_returns401() throws Exception {
        when(authService.login(any())).thenThrow(
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        LoginRequest req = new LoginRequest("alice@example.com", "WrongPassword!");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // POST /auth/logout
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001")
    @DisplayName("POST /auth/logout — authenticated user → 204 No Content")
    void logout_authenticatedUser_returns204() throws Exception {
        doNothing().when(authService).logout(any());

        mockMvc.perform(post("/auth/logout").with(csrf()))
                .andExpect(status().isNoContent());
    }
}
