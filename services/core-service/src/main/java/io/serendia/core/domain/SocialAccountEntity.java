package io.serendia.core.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "social_accounts",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_social_platform_user",
        columnNames = {"platform", "platform_user_id"}
    )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SocialAccountEntity {

    @Id @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SocialPlatform platform;

    @Column(name = "platform_user_id", nullable = false, length = 255)
    private String platformUserId;

    @Column(name = "display_name", length = 255)
    private String displayName;

    /** AES-256-GCM encrypted. Never log or expose this field. */
    @Column(name = "access_token_enc", nullable = false, columnDefinition = "TEXT")
    private String accessTokenEnc;

    /** AES-256-GCM encrypted. Null for platforms that don't issue refresh tokens. */
    @Column(name = "refresh_token_enc", columnDefinition = "TEXT")
    private String refreshTokenEnc;

    @Column(name = "token_expires_at")
    private Instant tokenExpiresAt;

    @Column(columnDefinition = "TEXT")
    private String scopes;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "connected_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant connectedAt = Instant.now();
}
