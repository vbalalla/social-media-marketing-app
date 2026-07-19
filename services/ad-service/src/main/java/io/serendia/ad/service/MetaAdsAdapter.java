package io.serendia.ad.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Service
public class MetaAdsAdapter implements AdPlatformAdapter {
    private final Random random = new Random();

    @Override
    public String getPlatform() {
        return "META";
    }

    @Override
    public AdMetrics syncMetrics(String platformCampaignId) {
        int seed = platformCampaignId != null ? platformCampaignId.hashCode() : 42;
        random.setSeed(seed + System.currentTimeMillis() / 600000);

        double spend = 80.0 + random.nextDouble() * 150.0;
        double cpa = 1.50 + random.nextDouble() * 1.80; // CPA float: $1.50 - $3.30
        long clicks = (long) (spend / cpa);
        long impressions = clicks * 25;

        return new AdMetrics(
            BigDecimal.valueOf(spend).setScale(2, RoundingMode.HALF_UP),
            BigDecimal.valueOf(cpa).setScale(2, RoundingMode.HALF_UP),
            impressions,
            clicks
        );
    }

    @Override
    public boolean updateBudget(String platformCampaignId, BigDecimal newDailyBudget) {
        return true;
    }
}
