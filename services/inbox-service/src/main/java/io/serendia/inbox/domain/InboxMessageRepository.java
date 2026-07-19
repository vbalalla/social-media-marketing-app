package io.serendia.inbox.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InboxMessageRepository extends JpaRepository<InboxMessageEntity, UUID> {

    Page<InboxMessageEntity> findByWorkspaceId(UUID workspaceId, Pageable pageable);

    Page<InboxMessageEntity> findByWorkspaceIdAndPlatform(UUID workspaceId, String platform, Pageable pageable);

    Page<InboxMessageEntity> findByWorkspaceIdAndStatus(UUID workspaceId, String status, Pageable pageable);

    Page<InboxMessageEntity> findByWorkspaceIdAndPlatformAndStatus(UUID workspaceId, String platform, String status, Pageable pageable);

    boolean existsByPlatformAndPlatformMessageId(String platform, String platformMessageId);
}
