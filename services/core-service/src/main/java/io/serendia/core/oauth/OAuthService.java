package io.serendia.core.oauth;

import io.serendia.core.domain.SocialAccountEntity;
import io.serendia.core.domain.SocialAccountRepository;
import io.serendia.core.domain.SocialPlatform;
import io.serendia.core.service.TokenEncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Slf4j
public class OAuthService {

    private final OAuthStateService        stateService;
    private final MetaOAuthClient          metaClient;
    private final TikTokOAuthClient        tikTokClient;
    private final LinkedInOAuthClient      linkedinClient;
    private final XOAuthClient            xClient;
    private final SocialAccountRepository  socialAccountRepository;
    private final TokenEncryptionService   encryptionService;
    private final WebClient                internalWebClient;
    private final String                   internalSecret;

    public OAuthService(
            OAuthStateService stateService,
            MetaOAuthClient metaClient,
            TikTokOAuthClient tikTokClient,
            LinkedInOAuthClient linkedinClient,
            XOAuthClient xClient,
            SocialAccountRepository socialAccountRepository,
            TokenEncryptionService encryptionService,
            org.springframework.web.reactive.function.client.WebClient.Builder webClientBuilder,
            @org.springframework.beans.factory.annotation.Value("${auth-service.url}") String authServiceUrl,
            @org.springframework.beans.factory.annotation.Value("${internal.service-secret}") String internalSecret
    ) {
        this.stateService = stateService;
        this.metaClient = metaClient;
        this.tikTokClient = tikTokClient;
        this.linkedinClient = linkedinClient;
        this.xClient = xClient;
        this.socialAccountRepository = socialAccountRepository;
        this.encryptionService = encryptionService;
        this.internalWebClient = webClientBuilder.baseUrl(authServiceUrl).build();
        this.internalSecret = internalSecret;
    }

    // -------------------------------------------------------------------------
    // Initiate OAuth flow
    // -------------------------------------------------------------------------

    public String initOAuth(UUID workspaceId, UUID userId, String platform) {
        SocialPlatform p = parsePlatform(platform);
        String codeVerifier  = PkceUtil.generateCodeVerifier();
        String codeChallenge = PkceUtil.generateCodeChallenge(codeVerifier);
        String state         = stateService.createState(workspaceId, userId, platform, codeVerifier);

        return switch (p) {
            case FACEBOOK, INSTAGRAM -> metaClient.buildAuthorizationUrl(state, codeChallenge);
            case TIKTOK              -> tikTokClient.buildAuthorizationUrl(state, codeChallenge);
            case LINKEDIN            -> linkedinClient.buildAuthorizationUrl(state, codeChallenge);
            case X                   -> xClient.buildAuthorizationUrl(state, codeChallenge);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "OAuth flow not supported for platform: " + platform);
        };
    }

    // -------------------------------------------------------------------------
    // Handle OAuth callback
    // -------------------------------------------------------------------------

    @Transactional
    public ConnectedAccountInfo handleCallback(String code, String state) {
        OAuthStateService.OAuthStatePayload payload = stateService.validateAndConsume(state);
        SocialPlatform platform = parsePlatform(payload.platform());

        ConnectedAccountInfo info = switch (platform) {
            case FACEBOOK, INSTAGRAM -> handleMetaCallback(code, payload);
            case TIKTOK              -> handleTikTokCallback(code, payload);
            case LINKEDIN            -> handleLinkedInCallback(code, payload);
            case X                   -> handleXCallback(code, payload);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unsupported platform: " + payload.platform());
        };

        // Notify auth-service that onboarding is complete for this user
        triggerOnboardingComplete(payload.userId());

        return info;
    }

    // -------------------------------------------------------------------------
    // Meta callback
    // -------------------------------------------------------------------------

    private ConnectedAccountInfo handleMetaCallback(String code, OAuthStateService.OAuthStatePayload payload) {
        MetaOAuthClient.MetaTokenResponse tokens = metaClient.exchangeCode(code, payload.codeVerifier());
        MetaOAuthClient.MetaUserInfo me = metaClient.getMe(tokens.access_token());

        String accessEnc  = encryptionService.encrypt(tokens.access_token());
        String refreshEnc = tokens.refresh_token() != null
                ? encryptionService.encrypt(tokens.refresh_token()) : null;

        SocialPlatform platform = parsePlatform(payload.platform());
        SocialAccountEntity account = SocialAccountEntity.builder()
                .workspaceId(payload.workspaceId())
                .platform(platform)
                .platformUserId(me.id())
                .displayName(me.name())
                .accessTokenEnc(accessEnc)
                .refreshTokenEnc(refreshEnc)
                .tokenExpiresAt(tokens.expires_in() != null
                        ? Instant.now().plus(tokens.expires_in(), ChronoUnit.SECONDS) : null)
                .build();

        SocialAccountEntity saved = socialAccountRepository.save(account);
        log.info("Connected {} account {} for workspace {}", platform, me.id(), payload.workspaceId());
        return new ConnectedAccountInfo(saved.getId(), me.name(), platform.name());
    }

    // -------------------------------------------------------------------------
    // TikTok callback
    // -------------------------------------------------------------------------

    private ConnectedAccountInfo handleTikTokCallback(String code, OAuthStateService.OAuthStatePayload payload) {
        TikTokOAuthClient.TikTokTokenResponse tokens = tikTokClient.exchangeCode(code, payload.codeVerifier());

        String accessEnc  = encryptionService.encrypt(tokens.access_token());
        String refreshEnc = tokens.refresh_token() != null
                ? encryptionService.encrypt(tokens.refresh_token()) : null;

        SocialAccountEntity account = SocialAccountEntity.builder()
                .workspaceId(payload.workspaceId())
                .platform(SocialPlatform.TIKTOK)
                .platformUserId(tokens.open_id())
                .displayName("TikTok " + tokens.open_id())
                .accessTokenEnc(accessEnc)
                .refreshTokenEnc(refreshEnc)
                .scopes(tokens.scope())
                .tokenExpiresAt(tokens.expires_in() != null
                        ? Instant.now().plus(tokens.expires_in(), ChronoUnit.SECONDS) : null)
                .build();

        SocialAccountEntity saved = socialAccountRepository.save(account);
        log.info("Connected TikTok account {} for workspace {}", tokens.open_id(), payload.workspaceId());
        return new ConnectedAccountInfo(saved.getId(), saved.getDisplayName(), "TIKTOK");
    }

    // -------------------------------------------------------------------------
    // LinkedIn callback
    // -------------------------------------------------------------------------

    private ConnectedAccountInfo handleLinkedInCallback(String code, OAuthStateService.OAuthStatePayload payload) {
        LinkedInOAuthClient.LinkedInTokenResponse tokens = linkedinClient.exchangeCode(code, payload.codeVerifier());
        LinkedInOAuthClient.LinkedInUserInfo me = linkedinClient.getMe(tokens.access_token());

        String accessEnc  = encryptionService.encrypt(tokens.access_token());
        String refreshEnc = tokens.refresh_token() != null
                ? encryptionService.encrypt(tokens.refresh_token()) : null;

        SocialAccountEntity account = SocialAccountEntity.builder()
                .workspaceId(payload.workspaceId())
                .platform(SocialPlatform.LINKEDIN)
                .platformUserId(me.id())
                .displayName(me.getFullName())
                .accessTokenEnc(accessEnc)
                .refreshTokenEnc(refreshEnc)
                .tokenExpiresAt(tokens.expires_in() != null
                        ? Instant.now().plus(tokens.expires_in(), ChronoUnit.SECONDS) : null)
                .build();

        SocialAccountEntity saved = socialAccountRepository.save(account);
        log.info("Connected LinkedIn account {} for workspace {}", me.id(), payload.workspaceId());
        return new ConnectedAccountInfo(saved.getId(), saved.getDisplayName(), "LINKEDIN");
    }

    // -------------------------------------------------------------------------
    // X callback
    // -------------------------------------------------------------------------

    private ConnectedAccountInfo handleXCallback(String code, OAuthStateService.OAuthStatePayload payload) {
        XOAuthClient.XTokenResponse tokens = xClient.exchangeCode(code, payload.codeVerifier());
        XOAuthClient.XUserInfo me = xClient.getMe(tokens.access_token());

        String accessEnc  = encryptionService.encrypt(tokens.access_token());
        String refreshEnc = tokens.refresh_token() != null
                ? encryptionService.encrypt(tokens.refresh_token()) : null;

        String platformUserId = me != null ? me.id() : "x-user";
        String displayName = me != null ? "@" + me.username() : "X Account";

        SocialAccountEntity account = SocialAccountEntity.builder()
                .workspaceId(payload.workspaceId())
                .platform(SocialPlatform.X)
                .platformUserId(platformUserId)
                .displayName(displayName)
                .accessTokenEnc(accessEnc)
                .refreshTokenEnc(refreshEnc)
                .scopes(tokens.scope())
                .tokenExpiresAt(tokens.expires_in() != null
                        ? Instant.now().plus(tokens.expires_in(), ChronoUnit.SECONDS) : null)
                .build();

        SocialAccountEntity saved = socialAccountRepository.save(account);
        log.info("Connected X account {} for workspace {}", platformUserId, payload.workspaceId());
        return new ConnectedAccountInfo(saved.getId(), saved.getDisplayName(), "X");
    }

    // -------------------------------------------------------------------------
    // Helpers & Internal Service Calls
    // -------------------------------------------------------------------------

    private void triggerOnboardingComplete(UUID userId) {
        try {
            internalWebClient.patch()
                    .uri("/auth/users/{userId}/onboarding-complete", userId)
                    .header("X-Internal-Secret", internalSecret)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Successfully updated onboarding state for user {} in auth-service", userId);
        } catch (Exception e) {
            log.error("Failed to notify auth-service about completed onboarding for user {}", userId, e);
        }
    }

    private SocialPlatform parsePlatform(String platform) {
        try {
            if ("META".equalsIgnoreCase(platform)) {
                return SocialPlatform.FACEBOOK;
            }
            if ("TWITTER".equalsIgnoreCase(platform)) {
                return SocialPlatform.X;
            }
            return SocialPlatform.valueOf(platform.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown platform: " + platform);
        }
    }

    public record ConnectedAccountInfo(UUID socialAccountId, String displayName, String platform) {}
}
