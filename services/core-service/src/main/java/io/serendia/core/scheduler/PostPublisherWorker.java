package io.serendia.core.scheduler;

import io.serendia.core.domain.*;
import io.serendia.core.service.PlatformAdapterRegistry;
import io.serendia.core.service.SocialPlatformAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostPublisherWorker {

    private final PostRepository           postRepository;
    private final SocialAccountRepository  socialAccountRepository;
    private final PlatformAdapterRegistry  adapterRegistry;

    @Scheduled(fixedDelay = 10000, initialDelay = 10000)
    @Transactional
    public void publishPendingPosts() {
        List<PostEntity> ready = postRepository.findReadyToPublish(Instant.now());
        if (ready.isEmpty()) {
            return;
        }

        log.info("Found {} posts ready for publication", ready.size());

        for (PostEntity post : ready) {
            try {
                processPost(post);
            } catch (Exception e) {
                log.error("Error processing post {}: {}", post.getId(), e.getMessage());
                post.setStatus("FAILED");
                postRepository.save(post);
            }
        }
    }

    private void processPost(PostEntity post) {
        log.info("Processing post {} in workspace {}", post.getId(), post.getWorkspaceId());

        // Find active social accounts for this workspace
        List<SocialAccountEntity> accounts = socialAccountRepository.findByWorkspaceIdAndIsActiveTrue(post.getWorkspaceId());

        boolean allSucceeded = true;
        for (PostPlatformTargetEntity target : post.getTargets()) {
            // Find active account for this platform
            SocialAccountEntity account = accounts.stream()
                    .filter(a -> a.getPlatform() == target.getPlatform())
                    .findFirst()
                    .orElse(null);

            if (account == null) {
                String error = "No active connected account found for platform " + target.getPlatform();
                log.warn("Post {}: {}", post.getId(), error);
                target.setStatus("FAILED");
                target.setErrorMessage(error);
                allSucceeded = false;
                continue;
            }

            SocialPlatformAdapter adapter = adapterRegistry.getAdapter(target.getPlatform()).orElse(null);
            if (adapter == null) {
                String error = "No adapter registered for platform " + target.getPlatform();
                log.error("Post {}: {}", post.getId(), error);
                target.setStatus("FAILED");
                target.setErrorMessage(error);
                allSucceeded = false;
                continue;
            }

            try {
                String nativeId = adapter.publishPost(account, post.getContent(), post.getMediaUrls());
                target.setStatus("SUCCESS");
                target.setNativePostId(nativeId);
                target.setPublishedAt(Instant.now());
                target.setErrorMessage(null);
                log.info("Post {} successfully published to {} with ID {}", post.getId(), target.getPlatform(), nativeId);
            } catch (Exception e) {
                log.error("Failed to publish post {} to {}: {}", post.getId(), target.getPlatform(), e.getMessage());
                target.setStatus("FAILED");
                target.setErrorMessage(e.getMessage());
                allSucceeded = false;
            }
        }

        post.setStatus(allSucceeded ? "PUBLISHED" : "FAILED");
        post.setUpdatedAt(Instant.now());
        postRepository.save(post);
    }
}
