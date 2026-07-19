package io.serendia.core.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "post_platform_targets")
@IdClass(PostPlatformTargetId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostPlatformTargetEntity {

    @Id
    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SocialPlatform platform;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING"; // PENDING, SUCCESS, FAILED

    @Column(name = "native_post_id", length = 255)
    private String nativePostId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "published_at")
    private Instant publishedAt;
}
