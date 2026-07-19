package io.serendia.core.service;

import io.serendia.core.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostSchedulerService {

    private final PostRepository postRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    @Transactional
    public PostEntity createOrSchedulePost(UUID workspaceId, UUID userId, String content, List<String> mediaUrls, List<SocialPlatform> platforms, Instant scheduledAt) {
        // Workspace membership check
        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        if (!workspace.getOwnerId().equals(userId) &&
                !workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to workspace");
        }

        String initialStatus = (scheduledAt != null && scheduledAt.isAfter(Instant.now())) ? "SCHEDULED" : "PUBLISHING";

        PostEntity post = PostEntity.builder()
                .workspaceId(workspaceId)
                .content(content)
                .mediaUrls(mediaUrls != null ? mediaUrls : new ArrayList<>())
                .status(initialStatus)
                .scheduledAt(scheduledAt)
                .build();

        // Create platform targets
        List<PostPlatformTargetEntity> targets = platforms.stream()
                .map(platform -> PostPlatformTargetEntity.builder()
                        .postId(post.getId())
                        .platform(platform)
                        .status("PENDING")
                        .build())
                .toList();
        post.getTargets().addAll(targets);

        PostEntity saved = postRepository.save(post);
        log.info("Post {} created in workspace {} with status {}", saved.getId(), workspaceId, initialStatus);

        return saved;
    }

    @Transactional
    public void cancelPost(UUID postId, UUID userId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        WorkspaceEntity workspace = workspaceRepository.findById(post.getWorkspaceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        if (!workspace.getOwnerId().equals(userId) &&
                !workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspace.getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to workspace");
        }

        if (!"SCHEDULED".equals(post.getStatus()) && !"DRAFT".equals(post.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only SCHEDULED or DRAFT posts can be cancelled");
        }

        post.setStatus("CANCELLED");
        postRepository.save(post);
        log.info("Cancelled post {}", postId);
    }
}
