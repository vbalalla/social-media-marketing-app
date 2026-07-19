package io.serendia.ad.service;

import io.serendia.ad.domain.CampaignEntity;
import io.serendia.ad.domain.CampaignPlatformConfigEntity;
import io.serendia.ad.domain.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsSyncScheduler {

    private final CampaignRepository campaignRepository;
    private final AdPlatformAdapterRegistry adapterRegistry;

    @Scheduled(fixedDelay = 900000) // Every 15 minutes
    @Transactional
    public void syncAllMetrics() {
        log.info("Starting paid ad campaign metrics synchronization...");
        List<CampaignEntity> campaigns = campaignRepository.findAll();
        
        for (CampaignEntity campaign : campaigns) {
            if ("COMPLETED".equals(campaign.getStatus())) {
                continue;
            }
            
            for (CampaignPlatformConfigEntity config : campaign.getPlatformConfigs()) {
                adapterRegistry.getAdapter(config.getPlatform()).ifPresentOrElse(
                    adapter -> {
                        try {
                            AdMetrics metrics = adapter.syncMetrics(config.getPlatformCampaignId());
                            config.setSpendUsd(metrics.getSpendUsd());
                            config.setCpaUsd(metrics.getCpaUsd());
                            config.setImpressions(metrics.getImpressions());
                            config.setClicks(metrics.getClicks());
                            config.setLastSyncedAt(Instant.now());
                            config.setUpdatedAt(Instant.now());
                            log.debug("Synced campaign {} platform {} metrics: spend={}, cpa={}", 
                                campaign.getId(), config.getPlatform(), metrics.getSpendUsd(), metrics.getCpaUsd());
                        } catch (Exception e) {
                            log.error("Failed to sync metrics for campaign {} platform {}: {}", 
                                campaign.getId(), config.getPlatform(), e.getMessage());
                        }
                    },
                    () -> log.warn("No adapter found for platform {}", config.getPlatform())
                );
            }
            campaign.setUpdatedAt(Instant.now());
            campaignRepository.save(campaign);
        }
        log.info("Campaign metrics sync complete.");
    }
}
