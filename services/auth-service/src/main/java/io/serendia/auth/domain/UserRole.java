package io.serendia.auth.domain;

/**
 * Platform-wide user roles. Workspace-level roles are stored separately
 * in WORKSPACE_MEMBERS (core-service), but a user's global role controls
 * admin-level platform access.
 */
public enum UserRole {
    OWNER,   // Workspace owner — full control
    ADMIN,   // Can manage workspace members and settings
    MEMBER,  // Standard user — create/publish content
    VIEWER   // Read-only access
}
