package io.serendia.core.oauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * HTTP client for the Meta OAuth 2.0 flow (Facebook + Instagram).
 * Uses Spring WebClient (non-blocking) for token exchanges.
 */
@Component
@Slf4j
public class MetaOAuthClient {

    private static final String GRAPH_BASE  = "https://graph.facebook.com";
    private static final String OAUTH_TOKEN = GRAPH_BASE + "/oauth/access_token";

    private final WebClient  webClient;
    private final String     appId;
    private final String     appSecret;
    private final String     redirectUri;

    public MetaOAuthClient(
            WebClient.Builder builder,
            @Value("${meta.app-id}")       String appId,
            @Value("${meta.app-secret}")   String appSecret,
            @Value("${meta.redirect-uri}") String redirectUri
    ) {
        this.webClient   = builder.build();
        this.appId       = appId;
        this.appSecret   = appSecret;
        this.redirectUri = redirectUri;
    }

    /**
     * Builds the Meta authorization URL with PKCE parameters.
     *
     * @param state         CSRF state token
     * @param codeChallenge PKCE S256 code challenge
     */
    public String buildAuthorizationUrl(String state, String codeChallenge) {
        return UriComponentsBuilder.fromHttpUrl(GRAPH_BASE + "/dialog/oauth")
                .queryParam("client_id",             appId)
                .queryParam("redirect_uri",           redirectUri)
                .queryParam("scope",                  "pages_manage_posts,pages_read_engagement,ads_management,instagram_basic,instagram_manage_messages")
                .queryParam("response_type",          "code")
                .queryParam("state",                  state)
                .queryParam("code_challenge",         codeChallenge)
                .queryParam("code_challenge_method",  "S256")
                .build()
                .toUriString();
    }

    /**
     * Exchanges the authorization code for access and refresh tokens.
     */
    public MetaTokenResponse exchangeCode(String code, String codeVerifier) {
        return webClient.post()
                .uri(OAUTH_TOKEN)
                .body(BodyInserters.fromFormData("client_id",     appId)
                        .with("client_secret",   appSecret)
                        .with("redirect_uri",    redirectUri)
                        .with("code",            code)
                        .with("code_verifier",   codeVerifier))
                .retrieve()
                .bodyToMono(MetaTokenResponse.class)
                .block(); // Blocking acceptable — controller is not on a reactive thread
    }

    /**
     * Fetches the Meta user profile to verify token validity.
     */
    public MetaUserInfo getMe(String accessToken) {
        return webClient.get()
                .uri(GRAPH_BASE + "/me?fields=id,name&access_token=" + accessToken)
                .retrieve()
                .bodyToMono(MetaUserInfo.class)
                .block();
    }

    // ---------------------------------------------------------------------------
    // Response records
    // ---------------------------------------------------------------------------

    public record MetaTokenResponse(
            String access_token,
            String token_type,
            Long   expires_in,
            String refresh_token
    ) {}

    public record MetaUserInfo(String id, String name) {}
}
