package io.serendia.ad.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReallocationEventRepository extends JpaRepository<ReallocationEventEntity, UUID> {
    List<ReallocationEventEntity> findByCampaignIdOrderByExecutedAtDesc(UUID campaignId);
}
