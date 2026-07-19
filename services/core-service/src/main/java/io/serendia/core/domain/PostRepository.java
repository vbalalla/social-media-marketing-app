package io.serendia.core.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, UUID> {

    Page<PostEntity> findByWorkspaceId(UUID workspaceId, Pageable pageable);

    Page<PostEntity> findByWorkspaceIdAndStatus(UUID workspaceId, String status, Pageable pageable);

    @Query("""
           SELECT p FROM PostEntity p
           WHERE p.status = 'PUBLISHING'
              OR (p.status = 'SCHEDULED' AND p.scheduledAt <= :now)
           """)
    List<PostEntity> findReadyToPublish(Instant now);
}
