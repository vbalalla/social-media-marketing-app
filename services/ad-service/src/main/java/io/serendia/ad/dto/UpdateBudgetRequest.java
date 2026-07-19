package io.serendia.ad.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateBudgetRequest {
    private BigDecimal dailyBudget;
}
