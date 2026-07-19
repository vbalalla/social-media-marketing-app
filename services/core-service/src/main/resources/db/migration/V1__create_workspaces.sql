-- =============================================================================
-- Core Service — Flyway V1: Workspaces
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE workspaces (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id    UUID         NOT NULL,   -- references auth-service users.id
    name        VARCHAR(255) NOT NULL,
    plan        VARCHAR(50)  NOT NULL DEFAULT 'FREE'
                    CHECK (plan IN ('FREE', 'STARTER', 'PRO', 'AGENCY')),
    settings    JSONB        NOT NULL DEFAULT '{}',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE workspace_members (
    workspace_id UUID        NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    user_id      UUID        NOT NULL,   -- references auth-service users.id
    role         VARCHAR(50) NOT NULL DEFAULT 'MEMBER'
                     CHECK (role IN ('ADMIN', 'MEMBER', 'VIEWER')),
    joined_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (workspace_id, user_id)
);

CREATE INDEX idx_workspaces_owner_id ON workspaces (owner_id);
CREATE INDEX idx_workspace_members_user ON workspace_members (user_id);

COMMENT ON TABLE workspaces         IS 'Multi-tenant workspace containers';
COMMENT ON TABLE workspace_members  IS 'User membership within a workspace';
