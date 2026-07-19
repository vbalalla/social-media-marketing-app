package io.serendia.core.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccountEntity, UUID> {

    List<SocialAccountEntity> findByWorkspaceIdAndIsActiveTrue(UUID workspaceId);

    boolean existsByPlatformAndPlatformUserId(SocialPlatform platform, String platformUserId);

    /** Finds active accounts whose tokens expire within the next 24 hours. */
    @Query("""
            SELECT s FROM SocialAccountEntity s
            WHERE s.isActive = true
              AND s.tokenExpiresAt IS NOT NULL
              AND s.tokenExpiresAt <= :threshold
            """)
    List<SocialAccountEntity> findExpiringTokens(Instant threshold);
}
