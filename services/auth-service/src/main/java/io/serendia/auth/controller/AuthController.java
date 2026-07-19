package io.serendia.auth.controller;

import io.serendia.auth.config.JwtProperties;
import io.serendia.auth.dto.AuthResponse;
import io.serendia.auth.dto.LoginRequest;
import io.serendia.auth.dto.RegisterRequest;
import io.serendia.auth.service.AuthService;
import io.serendia.auth.service.AuthService.AuthServiceTokenPair;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private static final String REFRESH_COOKIE = "serendia_rt";

    private final AuthService    authService;
    private final JwtProperties  jwtProps;

    // -------------------------------------------------------------------------
    // POST /auth/register
    // -------------------------------------------------------------------------

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // -------------------------------------------------------------------------
    // POST /auth/login
    // -------------------------------------------------------------------------

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req,
                                               HttpServletResponse response) {
        AuthServiceTokenPair pair = authService.login(req);
        setRefreshCookie(response, pair.rawRefreshToken(), (int) pair.refreshTokenTtl().getSeconds());
        return ResponseEntity.ok(pair.authResponse());
    }

    // -------------------------------------------------------------------------
    // POST /auth/refresh
    // -------------------------------------------------------------------------

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request,
                                                 HttpServletResponse response) {
        String refreshToken = extractRefreshCookie(request)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED,
                        "Refresh token cookie missing"));

        // User ID embedded in the cookie as {userId}:{token} is NOT used here.
        // Instead, we require it as a request header (X-User-Id forwarded by the gateway).
        // For direct calls, the refresh endpoint reads the user ID from the cookie prefix.
        // Format: "{userId}|{token}"
        String[] parts = refreshToken.split("\\|", 2);
        if (parts.length != 2) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid refresh token format");
        }

        UUID userId = UUID.fromString(parts[0]);
        String rawToken = parts[1];

        AuthServiceTokenPair pair = authService.refresh(userId, rawToken);
        setRefreshCookie(response, pair.rawRefreshToken(), (int) pair.refreshTokenTtl().getSeconds());
        return ResponseEntity.ok(pair.authResponse());
    }

    // -------------------------------------------------------------------------
    // POST /auth/logout
    // -------------------------------------------------------------------------

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal String userId,
                                        HttpServletResponse response) {
        authService.logout(UUID.fromString(userId));
        clearRefreshCookie(response);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Cookie helpers
    // -------------------------------------------------------------------------

    private void setRefreshCookie(HttpServletResponse response, String rawToken, int maxAgeSeconds) {
        Cookie cookie = new Cookie(REFRESH_COOKIE, rawToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);          // HTTPS only — use false for local HTTP dev
        cookie.setPath("/auth/refresh"); // Scoped: only sent on refresh endpoint
        cookie.setMaxAge(maxAgeSeconds);
        response.addCookie(cookie);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private Optional<String> extractRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
