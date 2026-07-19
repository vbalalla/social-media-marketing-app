package io.serendia.ad.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reallocation_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReallocationEventEntity {

    @Id @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Column(name = "from_platform", nullable = false, length = 50)
    private String fromPlatform;

    @Column(name = "to_platform", nullable = false, length = 50)
    private String toPlatform;

    @Column(name = "amount_usd", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountUsd;

    @Column(name = "trigger_cpa_delta", nullable = false, precision = 5, scale = 2)
    private BigDecimal triggerCpaDelta;

    @Column(name = "executed_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant executedAt = Instant.now();
}
