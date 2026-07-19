package io.serendia.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PasswordHashingService — Unit Tests")
class PasswordHashingServiceTest {

    private final PasswordHashingService service = new PasswordHashingService();

    @Test
    @DisplayName("hash returns a BCrypt string (starts with $2a$ or $2b$)")
    void hash_returnsBcryptString() {
        String hash = service.hash("MySecretPassword123");
        assertThat(hash).startsWith("$2");
        assertThat(hash).hasSizeGreaterThan(50);
    }

    @Test
    @DisplayName("matches returns true for correct password")
    void matches_correctPassword_returnsTrue() {
        String hash = service.hash("MySecretPassword123");
        assertThat(service.matches("MySecretPassword123", hash)).isTrue();
    }

    @Test
    @DisplayName("matches returns false for wrong password")
    void matches_wrongPassword_returnsFalse() {
        String hash = service.hash("MySecretPassword123");
        assertThat(service.matches("WrongPassword999", hash)).isFalse();
    }

    @Test
    @DisplayName("same password produces different hashes (random salt)")
    void hash_sameInput_producesUniqueHashes() {
        String hash1 = service.hash("SamePassword123");
        String hash2 = service.hash("SamePassword123");
        assertThat(hash1).isNotEqualTo(hash2);
        // But both should match
        assertThat(service.matches("SamePassword123", hash1)).isTrue();
        assertThat(service.matches("SamePassword123", hash2)).isTrue();
    }
}
