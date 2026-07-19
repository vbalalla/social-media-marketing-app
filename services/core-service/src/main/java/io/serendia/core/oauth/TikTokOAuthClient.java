package io.serendia.core.oauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * HTTP client for the TikTok OAuth 2.0 flow.
 * TikTok uses a non-standard OAuth flow with a different token endpoint.
 *
 * <p>Note: TikTok access tokens expire in 24 hours; refresh tokens last 365 days.
 * An hourly refresh job handles renewal.
 */
@Component
@Slf4j
public class TikTokOAuthClient {

    private static final String TIKTOK_AUTH_BASE  = "https://www.tiktok.com";
    private static final String TIKTOK_API_BASE   = "https://open.tiktokapis.com";
    private static final String TOKEN_ENDPOINT    = TIKTOK_API_BASE + "/v2/oauth/token/";

    private final WebClient webClient;
    private final String    clientKey;
    private final String    clientSecret;
    private final String    redirectUri;

    public TikTokOAuthClient(
            WebClient.Builder builder,
            @Value("${tiktok.client-key}")    String clientKey,
            @Value("${tiktok.client-secret}") String clientSecret,
            @Value("${tiktok.redirect-uri}")  String redirectUri
    ) {
        this.webClient    = builder.build();
        this.clientKey    = clientKey;
        this.clientSecret = clientSecret;
        this.redirectUri  = redirectUri;
    }

    public String buildAuthorizationUrl(String state, String codeChallenge) {
        return UriComponentsBuilder.fromHttpUrl(TIKTOK_AUTH_BASE + "/v2/auth/authorize/")
                .queryParam("client_key",            clientKey)
                .queryParam("scope",                 "user.info.basic,video.list")
                .queryParam("response_type",         "code")
                .queryParam("redirect_uri",          redirectUri)
                .queryParam("state",                 state)
                .queryParam("code_challenge",        codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .build()
                .toUriString();
    }

    public TikTokTokenResponse exchangeCode(String code, String codeVerifier) {
        return webClient.post()
                .uri(TOKEN_ENDPOINT)
                .body(BodyInserters.fromFormData("client_key",    clientKey)
                        .with("client_secret", clientSecret)
                        .with("code",          code)
                        .with("grant_type",    "authorization_code")
                        .with("redirect_uri",  redirectUri)
                        .with("code_verifier", codeVerifier))
                .retrieve()
                .bodyToMono(TikTokTokenResponse.class)
                .block();
    }

    public TikTokTokenResponse refreshToken(String refreshToken) {
        return webClient.post()
                .uri(TOKEN_ENDPOINT)
                .body(BodyInserters.fromFormData("client_key",     clientKey)
                        .with("client_secret",  clientSecret)
                        .with("grant_type",     "refresh_token")
                        .with("refresh_token",  refreshToken))
                .retrieve()
                .bodyToMono(TikTokTokenResponse.class)
                .block();
    }

    public record TikTokTokenResponse(
            String access_token,
            String refresh_token,
            Long   expires_in,
            Long   refresh_expires_in,
            String open_id,
            String scope
    ) {}
}
