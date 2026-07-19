package io.serendia.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

/**
 * Manages opaque refresh tokens stored in Redis.
 * The raw token is sent to the client as an HttpOnly cookie;
 * only its SHA-256 hash is persisted in Redis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refresh:";
    private static final int TOKEN_BYTES = 64;

    private final StringRedisTemplate redis;

    /**
     * Generates a new refresh token, stores its hash in Redis, and returns the raw token.
     *
     * @param userId  owner user ID
     * @param ttl     time-to-live for the Redis entry
     * @return raw opaque token (send to client)
     */
    public String createToken(UUID userId, Duration ttl) {
        byte[] raw = new byte[TOKEN_BYTES];
        new SecureRandom().nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        String hash  = sha256Hex(token);
        redis.opsForValue().set(KEY_PREFIX + userId, hash, ttl);
        return token;
    }

    /**
     * Validates the raw token against the stored hash.
     *
     * @return true if valid and not expired
     */
    public boolean isValid(UUID userId, String rawToken) {
        String stored = redis.opsForValue().get(KEY_PREFIX + userId);
        if (stored == null) return false;
        return MessageDigest.isEqual(
                sha256Hex(rawToken).getBytes(),
                stored.getBytes()
        );
    }

    /** Deletes the refresh token from Redis (logout / rotation). */
    public void revoke(UUID userId) {
        redis.delete(KEY_PREFIX + userId);
    }

    // ---------------------------------------------------------------------------
    // Helper
    // ---------------------------------------------------------------------------

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
