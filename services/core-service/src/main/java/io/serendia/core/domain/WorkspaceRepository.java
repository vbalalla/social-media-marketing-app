package io.serendia.core.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<WorkspaceEntity, UUID> {

    List<WorkspaceEntity> findByOwnerId(UUID ownerId);

    @Query("""
           SELECT w FROM WorkspaceEntity w
           WHERE w.ownerId = :userId
              OR w.id IN (SELECT wm.workspaceId FROM WorkspaceMemberEntity wm WHERE wm.userId = :userId)
           ORDER BY w.createdAt DESC
           """)
    List<WorkspaceEntity> findAllForUser(UUID userId);
}
