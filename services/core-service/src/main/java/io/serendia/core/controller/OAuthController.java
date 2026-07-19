package io.serendia.core.controller;

import io.serendia.core.oauth.OAuthService;
import io.serendia.core.oauth.OAuthService.ConnectedAccountInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
@Slf4j
public class OAuthController {

    private final OAuthService oAuthService;

    /**
     * GET /oauth/init?platform={META|INSTAGRAM|TIKTOK}&workspaceId={uuid}
     *
     * Returns the platform authorization URL. The SPA redirects the browser to this URL.
     */
    @GetMapping("/init")
    public ResponseEntity<Map<String, String>> initOAuth(
            @RequestParam String platform,
            @RequestParam UUID workspaceId,
            @AuthenticationPrincipal String userId   // Injected by JwtAuthenticationFilter
    ) {
        String authUrl = oAuthService.initOAuth(workspaceId, UUID.fromString(userId), platform);
        return ResponseEntity.ok(Map.of("authorizationUrl", authUrl));
    }

    /**
     * GET /oauth/callback?code={code}&state={state}
     *
     * Handles the redirect from the OAuth provider. On success, the browser is
     * redirected back to the SPA with the connected account info.
     *
     * Note: state param carries the platform identifier (stored in Redis).
     */
    @GetMapping("/callback")
    public ResponseEntity<ConnectedAccountInfo> handleCallback(
            @RequestParam String code,
            @RequestParam String state
    ) {
        ConnectedAccountInfo info = oAuthService.handleCallback(code, state);
        return ResponseEntity.status(HttpStatus.CREATED).body(info);
    }
}
