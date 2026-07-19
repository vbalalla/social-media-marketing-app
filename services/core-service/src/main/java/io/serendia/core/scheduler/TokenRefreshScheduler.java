package io.serendia.core.scheduler;

import io.serendia.core.domain.SocialAccountEntity;
import io.serendia.core.domain.SocialAccountRepository;
import io.serendia.core.oauth.MetaOAuthClient;
import io.serendia.core.oauth.TikTokOAuthClient;
import io.serendia.core.service.TokenEncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Background job that proactively refreshes OAuth tokens before they expire.
 * Runs every 6 hours and refreshes any token expiring within the next 24 hours.
 *
 * <p>Failure for one account is caught and logged — other accounts continue to refresh.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenRefreshScheduler {

    private static final long REFRESH_LEAD_HOURS = 24;

    private final SocialAccountRepository socialAccountRepository;
    private final TokenEncryptionService  encryptionService;
    private final MetaOAuthClient         metaClient;
    private final TikTokOAuthClient       tikTokClient;

    @Scheduled(fixedDelayString = "PT6H", initialDelayString = "PT5M")
    @Transactional
    public void refreshExpiringTokens() {
        Instant threshold = Instant.now().plus(REFRESH_LEAD_HOURS, ChronoUnit.HOURS);
        List<SocialAccountEntity> expiring = socialAccountRepository.findExpiringTokens(threshold);

        if (expiring.isEmpty()) {
            log.debug("Token refresh: no accounts expiring within {} hours", REFRESH_LEAD_HOURS);
            return;
        }

        log.info("Token refresh: found {} accounts to refresh", expiring.size());

        for (SocialAccountEntity account : expiring) {
            try {
                refreshAccount(account);
            } catch (Exception e) {
                log.error("Failed to refresh token for social account {} ({}): {}",
                        account.getId(), account.getPlatform(), e.getMessage());
                // Continue with remaining accounts
            }
        }
    }

    private void refreshAccount(SocialAccountEntity account) {
        if (account.getRefreshTokenEnc() == null) {
            log.warn("Account {} has no refresh token — marking inactive", account.getId());
            account.setActive(false);
            return;
        }

        String decryptedRefresh = encryptionService.decrypt(account.getRefreshTokenEnc());

        switch (account.getPlatform()) {
            case FACEBOOK, INSTAGRAM -> refreshMetaToken(account, decryptedRefresh);
            case TIKTOK              -> refreshTikTokToken(account, decryptedRefresh);
            default -> log.warn("No refresh logic for platform {}", account.getPlatform());
        }
    }

    private void refreshMetaToken(SocialAccountEntity account, String refreshToken) {
        // Meta long-lived tokens can be extended via a specific endpoint
        // For simplicity, re-encrypt and extend expiry by 60 days (Meta LLT duration)
        // A full implementation would call: GET /oauth/access_token?grant_type=fb_exchange_token
        log.info("Meta token refresh not fully implemented — account {}", account.getId());
    }

    private void refreshTikTokToken(SocialAccountEntity account, String refreshToken) {
        TikTokOAuthClient.TikTokTokenResponse response = tikTokClient.refreshToken(refreshToken);

        account.setAccessTokenEnc(encryptionService.encrypt(response.access_token()));
        if (response.refresh_token() != null) {
            account.setRefreshTokenEnc(encryptionService.encrypt(response.refresh_token()));
        }
        account.setTokenExpiresAt(
                Instant.now().plus(response.expires_in(), ChronoUnit.SECONDS));

        log.info("Refreshed TikTok token for account {} — new expiry: {}",
                account.getId(), account.getTokenExpiresAt());
    }
}
