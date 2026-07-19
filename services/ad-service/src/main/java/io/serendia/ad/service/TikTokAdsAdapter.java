package io.serendia.ad.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Service
public class TikTokAdsAdapter implements AdPlatformAdapter {
    private final Random random = new Random();

    @Override
    public String getPlatform() {
        return "TIKTOK";
    }

    @Override
    public AdMetrics syncMetrics(String platformCampaignId) {
        int seed = platformCampaignId != null ? platformCampaignId.hashCode() : 99;
        random.setSeed(seed + System.currentTimeMillis() / 600000);

        double spend = 60.0 + random.nextDouble() * 180.0;
        double cpa = 2.10 + random.nextDouble() * 2.40; // CPA float: $2.10 - $4.50
        long clicks = (long) (spend / cpa);
        long impressions = clicks * 30;

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
