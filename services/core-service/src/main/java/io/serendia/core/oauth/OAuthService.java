package io.serendia.core.oauth;

import io.serendia.core.domain.SocialAccountEntity;
import io.serendia.core.domain.SocialAccountRepository;
import io.serendia.core.domain.SocialPlatform;
import io.serendia.core.service.TokenEncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private final OAuthStateService        stateService;
    private final MetaOAuthClient          metaClient;
    private final TikTokOAuthClient        tikTokClient;
    private final SocialAccountRepository  socialAccountRepository;
    private final TokenEncryptionService   encryptionService;

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

        return switch (platform) {
            case FACEBOOK, INSTAGRAM -> handleMetaCallback(code, payload);
            case TIKTOK              -> handleTikTokCallback(code, payload);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unsupported platform: " + payload.platform());
        };
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
    // Helpers
    // -------------------------------------------------------------------------

    private SocialPlatform parsePlatform(String platform) {
        try {
            if ("META".equalsIgnoreCase(platform)) {
                return SocialPlatform.FACEBOOK;
            }
            return SocialPlatform.valueOf(platform.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown platform: " + platform);
        }
    }

    public record ConnectedAccountInfo(UUID socialAccountId, String displayName, String platform) {}
}
