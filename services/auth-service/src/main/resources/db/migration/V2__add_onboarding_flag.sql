-- =============================================================================
-- Auth Service — Flyway Migration V2
-- Adds onboarding_complete flag to users table
-- =============================================================================

ALTER TABLE users ADD COLUMN onboarding_complete BOOLEAN NOT NULL DEFAULT FALSE;
