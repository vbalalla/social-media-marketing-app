-- =============================================================================
-- Core Service — Flyway V2: Social Accounts
-- =============================================================================

CREATE TABLE social_accounts (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id         UUID         NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    platform             VARCHAR(50)  NOT NULL
                             CHECK (platform IN ('FACEBOOK','INSTAGRAM','TIKTOK','LINKEDIN','X')),
    platform_user_id     VARCHAR(255) NOT NULL,
    display_name         VARCHAR(255),
    access_token_enc     TEXT         NOT NULL,  -- AES-256-GCM encrypted
    refresh_token_enc    TEXT,                   -- AES-256-GCM encrypted (nullable for platforms without refresh)
    token_expires_at     TIMESTAMPTZ,
    scopes               TEXT,
    is_active            BOOLEAN      NOT NULL DEFAULT TRUE,
    connected_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_social_platform_user UNIQUE (platform, platform_user_id)
);

CREATE INDEX idx_social_accounts_workspace ON social_accounts (workspace_id);
CREATE INDEX idx_social_accounts_expires   ON social_accounts (token_expires_at) WHERE is_active = TRUE;

COMMENT ON TABLE  social_accounts                IS 'OAuth-connected social media accounts per workspace';
COMMENT ON COLUMN social_accounts.access_token_enc  IS 'AES-256-GCM encrypted. Key stored in AWS Secrets Manager.';
COMMENT ON COLUMN social_accounts.refresh_token_enc IS 'AES-256-GCM encrypted. Null if platform does not issue refresh tokens.';
