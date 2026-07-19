package io.serendia.ad.service;

import io.serendia.ad.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetReallocationEngine {

    private final CampaignRepository campaignRepository;
    private final ReallocationEventRepository reallocationEventRepository;
    private final AdPlatformAdapterRegistry adapterRegistry;

    // Minimum budget allowed for a platform config to remain active
    private static final BigDecimal MIN_PLATFORM_BUDGET = BigDecimal.valueOf(5.00);

    @Scheduled(fixedDelay = 3600000) // Every 1 hour
    @Transactional
    public void executeReallocations() {
        log.info("Running automated ad budget reallocation engine...");
        List<CampaignEntity> campaigns = campaignRepository.findAll();

        for (CampaignEntity campaign : campaigns) {
            if (!"ACTIVE".equals(campaign.getStatus())) {
                continue;
            }

            List<CampaignPlatformConfigEntity> configs = campaign.getPlatformConfigs();
            if (configs.size() < 2) {
                continue; // Need at least two platform targets to shift budget
            }

            // Find config with lowest CPA and highest CPA
            CampaignPlatformConfigEntity bestConfig = null;
            CampaignPlatformConfigEntity worstConfig = null;

            for (CampaignPlatformConfigEntity config : configs) {
                if (config.getCpaUsd().compareTo(BigDecimal.ZERO) <= 0) {
                    continue; // Skip if metrics have not synced yet
                }

                if (bestConfig == null || config.getCpaUsd().compareTo(bestConfig.getCpaUsd()) < 0) {
                    bestConfig = config;
                }
                if (worstConfig == null || config.getCpaUsd().compareTo(worstConfig.getCpaUsd()) > 0) {
                    worstConfig = config;
                }
            }

            if (bestConfig == null || worstConfig == null || bestConfig == worstConfig) {
                continue;
            }

            BigDecimal lowCpa = bestConfig.getCpaUsd();
            BigDecimal highCpa = worstConfig.getCpaUsd();

            // CPA Delta = (High CPA - Low CPA) / High CPA
            BigDecimal delta = highCpa.subtract(lowCpa).divide(highCpa, 4, RoundingMode.HALF_UP);
            log.debug("Campaign {}: best platform = {} (CPA={}), worst platform = {} (CPA={}), delta={}",
                campaign.getId(), bestConfig.getPlatform(), lowCpa, worstConfig.getPlatform(), highCpa, delta);

            // If delta is greater than 20%
            if (delta.compareTo(BigDecimal.valueOf(0.20)) > 0) {
                BigDecimal currentWorstBudget = worstConfig.getDailyBudget();

                // Shift 30% of suboptimal platform budget
                BigDecimal shiftAmount = currentWorstBudget.multiply(BigDecimal.valueOf(0.30))
                    .setScale(2, RoundingMode.HALF_UP);

                // Check constraints: worst budget cannot drop below MIN_PLATFORM_BUDGET
                BigDecimal potentialNewWorstBudget = currentWorstBudget.subtract(shiftAmount);
                if (potentialNewWorstBudget.compareTo(MIN_PLATFORM_BUDGET) < 0) {
                    // Reduce shift amount to only drop to the minimum limit
                    shiftAmount = currentWorstBudget.subtract(MIN_PLATFORM_BUDGET);
                }

                if (shiftAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    log.debug("Skip budget shift: suboptimal platform budget already at minimum limit.");
                    continue;
                }

                final BigDecimal finalWorstBudget = currentWorstBudget.subtract(shiftAmount);
                final BigDecimal finalBestBudget = bestConfig.getDailyBudget().add(shiftAmount);
                final String worstCampaignId = worstConfig.getPlatformCampaignId();
                final String bestCampaignId = bestConfig.getPlatformCampaignId();

                log.info("Optimizing budget for campaign {} (CPA delta={}%): shifting ${} from {} to {}",
                    campaign.getName(), delta.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP),
                    shiftAmount, worstConfig.getPlatform(), bestConfig.getPlatform());

                // Update suboptimal platform
                worstConfig.setDailyBudget(finalWorstBudget);
                worstConfig.setUpdatedAt(Instant.now());
                adapterRegistry.getAdapter(worstConfig.getPlatform())
                    .ifPresent(a -> a.updateBudget(worstCampaignId, finalWorstBudget));

                // Update optimal platform
                bestConfig.setDailyBudget(finalBestBudget);
                bestConfig.setUpdatedAt(Instant.now());
                adapterRegistry.getAdapter(bestConfig.getPlatform())
                    .ifPresent(a -> a.updateBudget(bestCampaignId, finalBestBudget));

                // Save audit trail
                ReallocationEventEntity event = ReallocationEventEntity.builder()
                    .campaignId(campaign.getId())
                    .fromPlatform(worstConfig.getPlatform())
                    .toPlatform(bestConfig.getPlatform())
                    .amountUsd(shiftAmount)
                    .triggerCpaDelta(delta.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP))
                    .executedAt(Instant.now())
                    .build();

                reallocationEventRepository.save(event);
                campaignRepository.save(campaign);
            }
        }
        log.info("Ad budget reallocation engine execution complete.");
    }
}
