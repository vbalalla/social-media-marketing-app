package io.serendia.auth.dto;

import io.serendia.auth.domain.UserRole;

import java.util.UUID;

/**
 * Returned in the response body after successful login or token refresh.
 * The refresh token is NOT included here — it is set as an HttpOnly cookie.
 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UUID userId,
        String email,
        String fullName,
        UserRole role
) {
    public static AuthResponse of(String accessToken, long expiresInSeconds,
                                   UUID userId, String email, String fullName, UserRole role) {
        return new AuthResponse(accessToken, "Bearer", expiresInSeconds, userId, email, fullName, role);
    }
}
