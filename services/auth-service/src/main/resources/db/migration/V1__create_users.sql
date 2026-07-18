-- =============================================================================
-- Auth Service — Flyway Migration V1
-- Creates the USERS table
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    role            VARCHAR(50)  NOT NULL DEFAULT 'MEMBER'
                        CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER', 'VIEWER')),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    last_login_at   TIMESTAMPTZ,

    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_is_active ON users (is_active);

COMMENT ON TABLE  users             IS 'Platform users across all workspaces';
COMMENT ON COLUMN users.role        IS 'OWNER | ADMIN | MEMBER | VIEWER';
COMMENT ON COLUMN users.password_hash IS 'BCrypt hash (cost=12)';
