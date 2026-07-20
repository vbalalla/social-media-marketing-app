package io.serendia.ad.controller;

import io.serendia.ad.domain.*;
import io.serendia.ad.dto.CreateCampaignRequest;
import io.serendia.ad.dto.UpdateBudgetRequest;
import io.serendia.ad.dto.UpdateStatusRequest;
import io.serendia.ad.service.BudgetReallocationEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/ad")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignRepository campaignRepository;
    private final ReallocationEventRepository reallocationEventRepository;
    private final BudgetReallocationEngine budgetReallocationEngine;

    @PostMapping("/workspaces/{workspaceId}/campaigns")
    public ResponseEntity<CampaignEntity> createCampaign(
            @PathVariable UUID workspaceId,
            @RequestBody CreateCampaignRequest request) {

        BigDecimal budgetPerPlatform = request.getDailyBudget()
                .divide(BigDecimal.valueOf(request.getPlatforms().size()), 2, RoundingMode.HALF_UP);

        CampaignEntity campaign = CampaignEntity.builder()
                .workspaceId(workspaceId)
                .name(request.getName())
                .dailyBudget(request.getDailyBudget())
                .status("ACTIVE")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        CampaignEntity savedCampaign = campaignRepository.saveAndFlush(campaign);

        List<CampaignPlatformConfigEntity> configs = new ArrayList<>();
        for (String platform : request.getPlatforms()) {
            configs.add(CampaignPlatformConfigEntity.builder()
                    .campaignId(savedCampaign.getId())
                    .platform(platform.toUpperCase())
                    .platformCampaignId(platform.toLowerCase() + "_" + UUID.randomUUID().toString().substring(0, 8))
                    .dailyBudget(budgetPerPlatform)
                    .spendUsd(BigDecimal.ZERO)
                    .cpaUsd(BigDecimal.ZERO)
                    .impressions(0L)
                    .clicks(0L)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());
        }

        savedCampaign.setPlatformConfigs(configs);
        CampaignEntity saved = campaignRepository.saveAndFlush(savedCampaign);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/workspaces/{workspaceId}/campaigns")
    public ResponseEntity<List<CampaignEntity>> listCampaigns(@PathVariable UUID workspaceId) {
        List<CampaignEntity> campaigns = campaignRepository.findByWorkspaceId(workspaceId);
        return ResponseEntity.ok(campaigns);
    }

    @PatchMapping("/campaigns/{id}/status")
    public ResponseEntity<CampaignEntity> updateStatus(
            @PathVariable UUID id,
            @RequestBody UpdateStatusRequest request) {

        return campaignRepository.findById(id)
                .map(campaign -> {
                    campaign.setStatus(request.getStatus().toUpperCase());
                    campaign.setUpdatedAt(Instant.now());
                    CampaignEntity saved = campaignRepository.save(campaign);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/campaigns/{id}/budget")
    public ResponseEntity<CampaignEntity> updateBudget(
            @PathVariable UUID id,
            @RequestBody UpdateBudgetRequest request) {

        return campaignRepository.findById(id)
                .map(campaign -> {
                    campaign.setDailyBudget(request.getDailyBudget());
                    campaign.setUpdatedAt(Instant.now());
                    
                    List<CampaignPlatformConfigEntity> configs = campaign.getPlatformConfigs();
                    if (!configs.isEmpty()) {
                        BigDecimal budgetPerPlatform = request.getDailyBudget()
                                .divide(BigDecimal.valueOf(configs.size()), 2, RoundingMode.HALF_UP);
                        for (CampaignPlatformConfigEntity config : configs) {
                            config.setDailyBudget(budgetPerPlatform);
                            config.setUpdatedAt(Instant.now());
                        }
                    }
                    
                    CampaignEntity saved = campaignRepository.save(campaign);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/campaigns/{id}/reallocation-history")
    public ResponseEntity<List<ReallocationEventEntity>> getReallocationHistory(@PathVariable UUID id) {
        List<ReallocationEventEntity> history = reallocationEventRepository.findByCampaignIdOrderByExecutedAtDesc(id);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/campaigns/trigger-reallocation")
    public ResponseEntity<Void> triggerReallocation() {
        budgetReallocationEngine.executeReallocations();
        return ResponseEntity.ok().build();
    }
}
