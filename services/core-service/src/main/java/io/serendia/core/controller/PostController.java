package io.serendia.core.controller;

import io.serendia.core.domain.PostEntity;
import io.serendia.core.domain.SocialPlatform;
import io.serendia.core.domain.PostRepository;
import io.serendia.core.service.PostSchedulerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostSchedulerService postSchedulerService;
    private final PostRepository postRepository;

    @PostMapping("/workspaces/{workspaceId}/posts")
    public ResponseEntity<PostEntity> createPost(
            @PathVariable UUID workspaceId,
            @RequestBody @Valid CreatePostRequest request,
            Principal principal
    ) {
        UUID userId = UUID.fromString(principal.getName());
        PostEntity post = postSchedulerService.createOrSchedulePost(
                workspaceId,
                userId,
                request.content(),
                request.mediaUrls(),
                request.platforms(),
                request.scheduledAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @GetMapping("/workspaces/{workspaceId}/posts")
    public ResponseEntity<Page<PostEntity>> listPosts(
            @PathVariable UUID workspaceId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostEntity> posts;
        if (status != null && !status.isBlank()) {
            posts = postRepository.findByWorkspaceIdAndStatus(workspaceId, status.toUpperCase(), pageable);
        } else {
            posts = postRepository.findByWorkspaceId(workspaceId, pageable);
        }
        return ResponseEntity.ok(posts);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> cancelPost(
            @PathVariable UUID id,
            Principal principal
    ) {
        UUID userId = UUID.fromString(principal.getName());
        postSchedulerService.cancelPost(id, userId);
        return ResponseEntity.noContent().build();
    }

    public record CreatePostRequest(
            String content,
            List<String> mediaUrls,
            @NotEmpty List<SocialPlatform> platforms,
            Instant scheduledAt
    ) {}
}
