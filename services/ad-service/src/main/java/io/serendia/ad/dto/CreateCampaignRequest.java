package io.serendia.ad.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateCampaignRequest {
    private String name;
    private BigDecimal dailyBudget;
    private List<String> platforms; // META, TIKTOK
}
