package io.serendia.ad.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "campaign_platform_configs",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_campaign_platform",
        columnNames = {"campaign_id", "platform"}
    )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CampaignPlatformConfigEntity {

    @Id @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Column(nullable = false, length = 50)
    private String platform; // META, TIKTOK

    @Column(name = "platform_campaign_id", length = 255)
    private String platformCampaignId;

    @Column(name = "spend_usd", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal spendUsd = BigDecimal.ZERO;

    @Column(name = "cpa_usd", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal cpaUsd = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private long impressions = 0L;

    @Column(nullable = false)
    @Builder.Default
    private long clicks = 0L;

    @Column(name = "daily_budget", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyBudget;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
