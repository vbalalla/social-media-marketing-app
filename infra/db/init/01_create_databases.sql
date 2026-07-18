-- =============================================================================
-- Serendia Platform — PostgreSQL Initialization
-- Creates one database per microservice (schema isolation)
-- Runs automatically when the postgres Docker container first starts
-- =============================================================================

-- Auth Service database
CREATE DATABASE serendia_auth;

-- Core Service database (workspaces, posts, social accounts)
CREATE DATABASE serendia_core;

-- Ad Service database (campaigns, budgets, metrics)
CREATE DATABASE serendia_ad;

-- Inbox Service database (messages, labels, assignments)
CREATE DATABASE serendia_inbox;

-- Grant all privileges to the app user
GRANT ALL PRIVILEGES ON DATABASE serendia_auth   TO serendia;
GRANT ALL PRIVILEGES ON DATABASE serendia_core   TO serendia;
GRANT ALL PRIVILEGES ON DATABASE serendia_ad     TO serendia;
GRANT ALL PRIVILEGES ON DATABASE serendia_inbox  TO serendia;
