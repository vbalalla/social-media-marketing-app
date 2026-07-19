-- =============================================================================
-- Core Service — Flyway V3: Posts & Platform Targets
-- =============================================================================

CREATE TABLE posts (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID         NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    content      TEXT,
    media_urls   JSONB        NOT NULL DEFAULT '[]', -- JSON array of strings
    status       VARCHAR(50)  NOT NULL DEFAULT 'DRAFT'
                     CHECK (status IN ('DRAFT', 'SCHEDULED', 'PUBLISHING', 'PUBLISHED', 'FAILED', 'CANCELLED')),
    scheduled_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE post_platform_targets (
    post_id        UUID         NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    platform       VARCHAR(50)  NOT NULL
                       CHECK (platform IN ('FACEBOOK', 'INSTAGRAM', 'TIKTOK', 'LINKEDIN', 'X')),
    status         VARCHAR(50)  NOT NULL DEFAULT 'PENDING'
                       CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),
    native_post_id VARCHAR(255),
    error_message  TEXT,
    published_at   TIMESTAMPTZ,

    PRIMARY KEY (post_id, platform)
);

CREATE INDEX idx_posts_workspace ON posts (workspace_id);
CREATE INDEX idx_posts_scheduled ON posts (scheduled_at) WHERE status = 'SCHEDULED';

COMMENT ON TABLE posts                  IS 'Post entity that can be scheduled or drafted';
COMMENT ON TABLE post_platform_targets  IS 'Association table tracking native post publication status per platform';
