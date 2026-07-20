package io.serendia.auth.service;

import io.serendia.auth.config.JwtProperties;
import io.serendia.auth.domain.UserEntity;
import io.serendia.auth.domain.UserRepository;
import io.serendia.auth.domain.UserRole;
import io.serendia.auth.dto.AuthResponse;
import io.serendia.auth.dto.LoginRequest;
import io.serendia.auth.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository     userRepository;
    private final PasswordHashingService passwordService;
    private final JwtService         jwtService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties      jwtProps;

    // ---------------------------------------------------------------------------
    // Registration
    // ---------------------------------------------------------------------------

    @Transactional
    public UserEntity register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "An account with this email address already exists");
        }

        UserEntity user = UserEntity.builder()
                .email(req.email().toLowerCase().trim())
                .passwordHash(passwordService.hash(req.password()))
                .fullName(req.fullName().trim())
                .role(UserRole.OWNER) // First registration creates workspace owner
                .build();

        return userRepository.save(user);
    }

    // ---------------------------------------------------------------------------
    // Login
    // ---------------------------------------------------------------------------

    @Transactional
    public AuthServiceTokenPair login(LoginRequest req) {
        UserEntity user = userRepository.findByEmail(req.email().toLowerCase().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"));

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account is deactivated");
        }

        if (!passwordService.matches(req.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        userRepository.updateLastLogin(user.getId(), Instant.now());

        return buildTokenPair(user);
    }

    // ---------------------------------------------------------------------------
    // Token Refresh
    // ---------------------------------------------------------------------------

    @Transactional
    public AuthServiceTokenPair refresh(UUID userId, String rawRefreshToken) {
        if (!refreshTokenService.isValid(userId, rawRefreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid or expired");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account is deactivated");
        }

        // Rotate: revoke old token, issue new pair
        refreshTokenService.revoke(userId);
        return buildTokenPair(user);
    }

    // ---------------------------------------------------------------------------
    // Logout
    // ---------------------------------------------------------------------------

    public void logout(UUID userId) {
        refreshTokenService.revoke(userId);
        log.info("User {} logged out, refresh token revoked", userId);
    }

    @Transactional
    public void completeOnboarding(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setOnboardingComplete(true);
        userRepository.save(user);
        log.info("User {} marked onboarding as complete", userId);
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private AuthServiceTokenPair buildTokenPair(UserEntity user) {
        String accessToken  = jwtService.issueAccessToken(user);
        Duration refreshTtl = Duration.ofDays(jwtProps.getRefreshTokenExpiryDays());
        String refreshToken = refreshTokenService.createToken(user.getId(), refreshTtl);

        AuthResponse response = AuthResponse.of(
                accessToken,
                jwtProps.getAccessTokenExpiryMinutes() * 60,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.isOnboardingComplete()
        );

        return new AuthServiceTokenPair(response, refreshToken, refreshTtl);
    }

    /**
     * Groups the access token response body with the raw refresh token
     * so the controller can set the HttpOnly cookie separately.
     */
    public record AuthServiceTokenPair(
            AuthResponse authResponse,
            String rawRefreshToken,
            Duration refreshTokenTtl
    ) {}
}
