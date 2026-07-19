-- =============================================================================
-- Ad Service — Flyway V1: Campaigns & Reallocations
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE campaigns (
    id           UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID           NOT NULL,
    name         VARCHAR(255)   NOT NULL,
    status       VARCHAR(50)    NOT NULL DEFAULT 'ACTIVE'
                     CHECK (status IN ('ACTIVE', 'PAUSED', 'COMPLETED')),
    daily_budget NUMERIC(10, 2) NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE campaign_platform_configs (
    id                   UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id          UUID           NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    platform             VARCHAR(50)    NOT NULL
                             CHECK (platform IN ('META', 'TIKTOK')),
    platform_campaign_id VARCHAR(255),
    spend_usd            NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    cpa_usd              NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    impressions          BIGINT         NOT NULL DEFAULT 0,
    clicks               BIGINT         NOT NULL DEFAULT 0,
    daily_budget         NUMERIC(10, 2) NOT NULL,
    last_synced_at       TIMESTAMPTZ,
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_campaign_platform UNIQUE (campaign_id, platform)
);

CREATE TABLE reallocation_events (
    id                UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id       UUID           NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    from_platform     VARCHAR(50)    NOT NULL,
    to_platform       VARCHAR(50)    NOT NULL,
    amount_usd        NUMERIC(10, 2) NOT NULL,
    trigger_cpa_delta NUMERIC(5, 2)  NOT NULL,
    executed_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_campaigns_workspace ON campaigns (workspace_id);
CREATE INDEX idx_campaign_platform_config ON campaign_platform_configs (campaign_id);

COMMENT ON TABLE campaigns IS 'Ad campaign master record';
COMMENT ON TABLE campaign_platform_configs IS 'Budget and real-time performance metrics split per platform';
COMMENT ON TABLE reallocation_events IS 'Audit logs for budget reallocation shifts calculated by the engine';
