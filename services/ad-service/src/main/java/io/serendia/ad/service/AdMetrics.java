package io.serendia.ad.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter @AllArgsConstructor
public class AdMetrics {
    private final BigDecimal spendUsd;
    private final BigDecimal cpaUsd;
    private final long impressions;
    private final long clicks;
}
