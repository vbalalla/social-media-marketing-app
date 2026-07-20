package io.serendia.core.oauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * HTTP client for the LinkedIn OAuth 2.0 flow.
 */
@Component
@Slf4j
public class LinkedInOAuthClient {

    private static final String AUTH_BASE  = "https://www.linkedin.com/oauth/v2/authorization";
    private static final String TOKEN_URL  = "https://www.linkedin.com/oauth/v2/accessToken";
    private static final String API_BASE    = "https://api.linkedin.com/v2";

    private final WebClient webClient;
    private final String    clientId;
    private final String    clientSecret;
    private final String    redirectUri;

    public LinkedInOAuthClient(
            WebClient.Builder builder,
            @Value("${linkedin.client-id}")       String clientId,
            @Value("${linkedin.client-secret}")   String clientSecret,
            @Value("${linkedin.redirect-uri}")   String redirectUri
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
                .queryParam("scope",                  "r_liteprofile r_emailaddress w_member_social")
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
    public LinkedInTokenResponse exchangeCode(String code, String codeVerifier) {
        return webClient.post()
                .uri(TOKEN_URL)
                .body(BodyInserters.fromFormData("grant_type",    "authorization_code")
                        .with("code",            code)
                        .with("redirect_uri",    redirectUri)
                        .with("client_id",       clientId)
                        .with("client_secret",   clientSecret)
                        .with("code_verifier",   codeVerifier))
                .retrieve()
                .bodyToMono(LinkedInTokenResponse.class)
                .block();
    }

    /**
     * Fetches profile name.
     */
    public LinkedInUserInfo getMe(String accessToken) {
        return webClient.get()
                .uri(API_BASE + "/me")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(LinkedInUserInfo.class)
                .block();
    }

    public record LinkedInTokenResponse(
            String access_token,
            Long   expires_in,
            String refresh_token
    ) {}

    public record LinkedInUserInfo(
            String id,
            String localizedFirstName,
            String localizedLastName
    ) {
        public String getFullName() {
            return (localizedFirstName != null ? localizedFirstName : "") +
                    (localizedLastName != null ? " " + localizedLastName : "");
        }
    }
}
