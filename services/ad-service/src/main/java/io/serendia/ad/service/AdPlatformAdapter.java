package io.serendia.ad.service;

import java.math.BigDecimal;

public interface AdPlatformAdapter {
    String getPlatform();
    AdMetrics syncMetrics(String platformCampaignId);
    boolean updateBudget(String platformCampaignId, BigDecimal newDailyBudget);
}
