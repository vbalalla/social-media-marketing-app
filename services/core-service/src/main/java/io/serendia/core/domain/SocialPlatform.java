package io.serendia.core.domain;

public enum SocialPlatform {
    FACEBOOK,
    INSTAGRAM,
    TIKTOK,
    LINKEDIN,
    X;

    public boolean supportsWebhooks() {
        return this == FACEBOOK || this == INSTAGRAM;
    }

    public boolean supportsRefreshToken() {
        return this == TIKTOK || this == LINKEDIN || this == X;
    }
}
