package io.serendia.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.serendia.auth.dto.LoginRequest;
import io.serendia.auth.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full Spring context integration test.
 * Starts a real PostgreSQL + Redis via Testcontainers.
 * Uses an ephemeral RSA key pair injected via DynamicPropertySource.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@DisplayName("Auth Service — Integration Tests")
class AuthIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("serendia_auth")
            .withUsername("serendia")
            .withPassword("test_password");

    @SuppressWarnings("resource")
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) throws Exception {
        // Real PostgreSQL
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username",  postgres::getUsername);
        registry.add("spring.datasource.password",  postgres::getPassword);

        // Real Redis
        registry.add("spring.data.redis.host",      () -> redis.getHost());
        registry.add("spring.data.redis.port",      () -> redis.getMappedPort(6379));
        registry.add("spring.data.redis.password",  () -> "");

        // Ephemeral RSA key pair
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        registry.add("jwt.private-key", () ->
                Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
        registry.add("jwt.public-key", () ->
                Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));

        registry.add("jwt.issuer",                         () -> "https://test.serendia.io");
        registry.add("jwt.access-token-expiry-minutes",   () -> "15");
        registry.add("jwt.refresh-token-expiry-days",     () -> "7");
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    // -------------------------------------------------------------------------

    @Test
    @Order(1)
    @DisplayName("Register a new user → 201")
    void register_newUser_returns201() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "integration@serendia.io", "IntegrationTest1!", "Integration User"
        );
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    @DisplayName("Login with correct credentials → 200 + JWT + HttpOnly cookie")
    void login_correctCredentials_returns200WithToken() throws Exception {
        // Ensure user exists first
        RegisterRequest reg = new RegisterRequest(
                "login_test@serendia.io", "IntegrationTest1!", "Login Test"
        );
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)));

        LoginRequest req = new LoginRequest("login_test@serendia.io", "IntegrationTest1!");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("login_test@serendia.io"))
                .andExpect(cookie().exists("serendia_rt"))
                .andExpect(cookie().httpOnly("serendia_rt", true));
    }

    @Test
    @Order(3)
    @DisplayName("Login with wrong password → 401")
    void login_wrongPassword_returns401() throws Exception {
        LoginRequest req = new LoginRequest("integration@serendia.io", "WrongPassword999!");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @Order(4)
    @DisplayName("Register duplicate email → 409 Conflict")
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "integration@serendia.io", "IntegrationTest1!", "Duplicate"
        );
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(5)
    @DisplayName("Actuator health endpoint is accessible without auth")
    void actuatorHealth_noAuth_returns200() throws Exception {
        mockMvc.perform(post("/actuator/health"))
                .andExpect(status().is(org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.is(200),
                        org.hamcrest.Matchers.is(405) // GET is the correct method
                )));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
