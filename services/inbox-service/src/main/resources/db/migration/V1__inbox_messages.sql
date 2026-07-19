-- =============================================================================
-- Inbox Service — Flyway V1: Inbox Messages
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE inbox_messages (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id        UUID         NOT NULL,
    platform            VARCHAR(50)  NOT NULL
                            CHECK (platform IN ('FACEBOOK', 'INSTAGRAM', 'TIKTOK', 'LINKEDIN', 'X')),
    platform_message_id VARCHAR(255) NOT NULL,
    sender_id           VARCHAR(255) NOT NULL,
    sender_name         VARCHAR(255),
    content             TEXT,
    sentiment           VARCHAR(50)  DEFAULT 'NEUTRAL'
                            CHECK (sentiment IN ('POSITIVE', 'NEUTRAL', 'NEGATIVE')),
    status              VARCHAR(50)  NOT NULL DEFAULT 'UNREAD'
                            CHECK (status IN ('UNREAD', 'READ', 'ARCHIVED')),
    assigned_to         UUID,        -- User ID assigned to message
    labels              JSONB        NOT NULL DEFAULT '[]', -- JSON array of label strings
    received_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_platform_msg UNIQUE (platform, platform_message_id)
);

CREATE INDEX idx_inbox_workspace ON inbox_messages (workspace_id);
CREATE INDEX idx_inbox_status    ON inbox_messages (workspace_id, status);
CREATE INDEX idx_inbox_received  ON inbox_messages (received_at DESC);

COMMENT ON TABLE inbox_messages IS 'Aggregated social platform customer direct messages and comments';
