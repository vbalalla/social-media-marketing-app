package io.serendia.inbox.controller;

import io.serendia.inbox.domain.InboxMessageEntity;
import io.serendia.inbox.domain.InboxMessageRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class InboxController {

    private final InboxMessageRepository inboxMessageRepository;

    @GetMapping("/workspaces/{workspaceId}/inbox")
    public ResponseEntity<Page<InboxMessageEntity>> getInbox(
            @PathVariable UUID workspaceId,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("receivedAt").descending());
        Page<InboxMessageEntity> messages;

        if (platform != null && !platform.isBlank() && status != null && !status.isBlank()) {
            messages = inboxMessageRepository.findByWorkspaceIdAndPlatformAndStatus(
                    workspaceId, platform.toUpperCase(), status.toUpperCase(), pageable);
        } else if (platform != null && !platform.isBlank()) {
            messages = inboxMessageRepository.findByWorkspaceIdAndPlatform(
                    workspaceId, platform.toUpperCase(), pageable);
        } else if (status != null && !status.isBlank()) {
            messages = inboxMessageRepository.findByWorkspaceIdAndStatus(
                    workspaceId, status.toUpperCase(), pageable);
        } else {
            messages = inboxMessageRepository.findByWorkspaceId(workspaceId, pageable);
        }

        return ResponseEntity.ok(messages);
    }

    @PatchMapping("/inbox/{id}/read")
    @Transactional
    public ResponseEntity<InboxMessageEntity> markAsRead(@PathVariable UUID id) {
        InboxMessageEntity msg = inboxMessageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));
        msg.setStatus("READ");
        msg.setUpdatedAt(Instant.now());
        return ResponseEntity.ok(inboxMessageRepository.save(msg));
    }

    @PatchMapping("/inbox/{id}/assign")
    @Transactional
    public ResponseEntity<InboxMessageEntity> assignUser(
            @PathVariable UUID id,
            @RequestBody @Valid AssignUserRequest request
    ) {
        InboxMessageEntity msg = inboxMessageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));
        msg.setAssignedTo(request.userId());
        msg.setUpdatedAt(Instant.now());
        return ResponseEntity.ok(inboxMessageRepository.save(msg));
    }

    @PostMapping("/inbox/{id}/labels")
    @Transactional
    public ResponseEntity<InboxMessageEntity> setLabels(
            @PathVariable UUID id,
            @RequestBody @Valid SetLabelsRequest request
    ) {
        InboxMessageEntity msg = inboxMessageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));
        msg.setLabels(new ArrayList<>(request.labels()));
        msg.setUpdatedAt(Instant.now());
        return ResponseEntity.ok(inboxMessageRepository.save(msg));
    }

    @PostMapping("/inbox/{id}/reply")
    @Transactional
    public ResponseEntity<Void> sendReply(
            @PathVariable UUID id,
            @RequestBody @Valid ReplyRequest request,
            Principal principal
    ) {
        InboxMessageEntity msg = inboxMessageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));

        log.info("User {} replying to inbox message {} ({}): text='{}'",
                principal.getName(), id, msg.getPlatform(), request.replyText());

        // Update status to read as reply is sent
        msg.setStatus("READ");
        msg.setUpdatedAt(Instant.now());
        inboxMessageRepository.save(msg);

        // TODO: Dispatch to core-service platform adapter over REST (Milestone 5 feature scope)
        return ResponseEntity.ok().build();
    }

    // DTO records
    public record AssignUserRequest(@NotNull UUID userId) {}
    public record SetLabelsRequest(@NotNull List<String> labels) {}
    public record ReplyRequest(@NotBlank String replyText) {}
}
