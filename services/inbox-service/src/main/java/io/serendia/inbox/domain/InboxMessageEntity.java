package io.serendia.inbox.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "inbox_messages",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_platform_msg",
        columnNames = {"platform", "platform_message_id"}
    )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InboxMessageEntity {

    @Id @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(nullable = false, length = 50)
    private String platform; // FACEBOOK, INSTAGRAM, TIKTOK, LINKEDIN, X

    @Column(name = "platform_message_id", nullable = false, length = 255)
    private String platformMessageId;

    @Column(name = "sender_id", nullable = false, length = 255)
    private String senderId;

    @Column(name = "sender_name", length = 255)
    private String senderName;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 50)
    @Builder.Default
    private String sentiment = "NEUTRAL"; // POSITIVE, NEUTRAL, NEGATIVE

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "UNREAD"; // UNREAD, READ, ARCHIVED

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "labels", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private List<String> labels = new ArrayList<>();

    @Column(name = "received_at", nullable = false)
    @Builder.Default
    private Instant receivedAt = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
