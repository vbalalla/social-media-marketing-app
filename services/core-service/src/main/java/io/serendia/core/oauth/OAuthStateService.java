package io.serendia.core.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

/**
 * Generates and validates OAuth state parameters (CSRF tokens) in Redis.
 * Also stores the PKCE code_verifier alongside the state for retrieval at callback time.
 *
 * Redis key: {@code oauth:state:{state}}
 * Value format: {@code {workspaceId}|{userId}|{platform}|{code_verifier}}
 * TTL: 10 minutes (matches OAuth authorization window)
 */
@Service
@RequiredArgsConstructor
public class OAuthStateService {

    private static final String KEY_PREFIX = "oauth:state:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redis;

    /**
     * Creates a new state entry and returns the random state parameter.
     */
    public String createState(UUID workspaceId, UUID userId, String platform, String codeVerifier) {
        byte[] stateBytes = new byte[32];
        new SecureRandom().nextBytes(stateBytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(stateBytes);

        String value = workspaceId + "|" + userId + "|" + platform + "|" + codeVerifier;
        redis.opsForValue().set(KEY_PREFIX + state, value, TTL);
        return state;
    }

    /**
     * Validates the state and returns its associated payload if valid.
     * The entry is deleted atomically after retrieval (one-time use).
     */
    public OAuthStatePayload validateAndConsume(String state) {
        String key   = KEY_PREFIX + state;
        String value = redis.opsForValue().getAndDelete(key);

        if (value == null) {
            throw new IllegalArgumentException("Invalid or expired OAuth state parameter");
        }

        String[] parts = value.split("\\|", 4);
        if (parts.length != 4) throw new IllegalStateException("Malformed state payload in Redis");

        return new OAuthStatePayload(
                UUID.fromString(parts[0]),
                UUID.fromString(parts[1]),
                parts[2],
                parts[3]
        );
    }

    public record OAuthStatePayload(
            UUID workspaceId,
            UUID userId,
            String platform,
            String codeVerifier
    ) {}
}
