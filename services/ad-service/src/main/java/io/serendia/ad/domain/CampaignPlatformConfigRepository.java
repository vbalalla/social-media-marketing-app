package io.serendia.ad.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CampaignPlatformConfigRepository extends JpaRepository<CampaignPlatformConfigEntity, UUID> {
    List<CampaignPlatformConfigEntity> findByCampaignId(UUID campaignId);
}
