package io.serendia.core.oauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * HTTP client for the X / Twitter OAuth 2.0 flow.
 */
@Component
@Slf4j
public class XOAuthClient {

    private static final String AUTH_BASE  = "https://twitter.com/i/oauth2/authorize";
    private static final String TOKEN_URL  = "https://api.twitter.com/2/oauth2/token";
    private static final String API_BASE    = "https://api.twitter.com/2";

    private final WebClient webClient;
    private final String    clientId;
    private final String    clientSecret;
    private final String    redirectUri;

    public XOAuthClient(
            WebClient.Builder builder,
            @Value("${x.client-id}")       String clientId,
            @Value("${x.client-secret}")   String clientSecret,
            @Value("${x.redirect-uri}")   String redirectUri
    ) {
        this.webClient    = builder.build();
        this.clientId     = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri  = redirectUri;
    }

    /**
     * Builds authorization URL.
     */
    public String buildAuthorizationUrl(String state, String codeChallenge) {
        return UriComponentsBuilder.fromHttpUrl(AUTH_BASE)
                .queryParam("client_id",             clientId)
                .queryParam("redirect_uri",           redirectUri)
                .queryParam("scope",                  "tweet.read tweet.write users.read offline.access")
                .queryParam("response_type",          "code")
                .queryParam("state",                  state)
                .queryParam("code_challenge",         codeChallenge)
                .queryParam("code_challenge_method",  "S256")
                .build()
                .toUriString();
    }

    /**
     * Exchanges code for tokens.
     */
    public XTokenResponse exchangeCode(String code, String codeVerifier) {
        String basicAuth = Base64.getEncoder().encodeToString(
                (clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8)
        );

        return webClient.post()
                .uri(TOKEN_URL)
                .header("Authorization", "Basic " + basicAuth)
                .body(BodyInserters.fromFormData("grant_type",    "authorization_code")
                        .with("code",            code)
                        .with("redirect_uri",    redirectUri)
                        .with("code_verifier",   codeVerifier))
                .retrieve()
                .bodyToMono(XTokenResponse.class)
                .block();
    }

    /**
     * Fetches user profile to verify token validity.
     */
    public XUserInfo getMe(String accessToken) {
        // GET /2/users/me returns a wrapper with data object
        XUserWrapper wrapper = webClient.get()
                .uri(API_BASE + "/users/me")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(XUserWrapper.class)
                .block();

        return wrapper != null ? wrapper.data() : null;
    }

    public record XTokenResponse(
            String access_token,
            Long   expires_in,
            String refresh_token,
            String scope
    ) {}

    public record XUserWrapper(XUserInfo data) {}

    public record XUserInfo(
            String id,
            String name,
            String username
    ) {}
}
