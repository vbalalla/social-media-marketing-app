package io.serendia.auth.service;

import io.serendia.auth.config.JwtProperties;
import io.serendia.auth.domain.UserEntity;
import io.serendia.auth.domain.UserRole;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("JwtService — Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private UserEntity testUser;

    @BeforeAll
    void setUp() throws Exception {
        // Generate an ephemeral RSA 2048 key pair for testing
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();

        String privateB64 = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());
        String publicB64  = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());

        JwtProperties props = new JwtProperties();
        props.setPrivateKey(privateB64);
        props.setPublicKey(publicB64);
        props.setIssuer("https://test.serendia.io");
        props.setAccessTokenExpiryMinutes(15);
        props.setRefreshTokenExpiryDays(7);

        jwtService = new JwtService(props);

        testUser = UserEntity.builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .email("alice@example.com")
                .fullName("Alice Test")
                .role(UserRole.ADMIN)
                .passwordHash("irrelevant")
                .build();
    }

    @Test
    @DisplayName("issueAccessToken returns a non-blank JWT string")
    void issueAccessToken_returnsNonBlankToken() {
        String token = jwtService.issueAccessToken(testUser);
        assertThat(token).isNotBlank();
        // JWT has 3 base64-encoded parts separated by dots
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("verifyAccessToken accepts a freshly-issued token")
    void verifyAccessToken_validToken_passes() {
        String token = jwtService.issueAccessToken(testUser);
        var decoded = jwtService.verifyAccessToken(token);

        assertThat(decoded.getSubject()).isEqualTo(testUser.getId().toString());
        assertThat(decoded.getClaim("uid").asString()).isEqualTo(testUser.getId().toString());
        assertThat(decoded.getClaim("role").asString()).isEqualTo("ADMIN");
        assertThat(decoded.getClaim("email").asString()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("verifyAccessToken rejects a tampered token")
    void verifyAccessToken_tamperedToken_throws() {
        String token = jwtService.issueAccessToken(testUser);
        // Corrupt the signature (last segment)
        String[] parts = token.split("\\.");
        String tampered = parts[0] + "." + parts[1] + ".invalidsignature";

        assertThatThrownBy(() -> jwtService.verifyAccessToken(tampered))
                .isInstanceOf(com.auth0.jwt.exceptions.JWTVerificationException.class);
    }

    @Test
    @DisplayName("verifyAccessToken rejects a token with wrong issuer")
    void verifyAccessToken_wrongIssuer_throws() throws Exception {
        // Create a separate JwtService with a different issuer
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();

        JwtProperties otherProps = new JwtProperties();
        otherProps.setPrivateKey(Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
        otherProps.setPublicKey(Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
        otherProps.setIssuer("https://evil.example.com");
        otherProps.setAccessTokenExpiryMinutes(15);
        otherProps.setRefreshTokenExpiryDays(7);

        JwtService otherService = new JwtService(otherProps);
        String foreignToken = otherService.issueAccessToken(testUser);

        // Our service should reject a token signed by a different key
        assertThatThrownBy(() -> jwtService.verifyAccessToken(foreignToken))
                .isInstanceOf(com.auth0.jwt.exceptions.JWTVerificationException.class);
    }
}
