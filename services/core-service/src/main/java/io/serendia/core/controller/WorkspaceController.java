package io.serendia.core.controller;

import io.serendia.core.domain.WorkspaceEntity;
import io.serendia.core.service.WorkspaceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/workspaces")
@RequiredArgsConstructor
@Slf4j
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<WorkspaceEntity> createWorkspace(
            @RequestBody @Valid CreateWorkspaceRequest request,
            Principal principal
    ) {
        UUID ownerId = UUID.fromString(principal.getName());
        WorkspaceEntity workspace = workspaceService.createWorkspace(ownerId, request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(workspace);
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceEntity>> listWorkspaces(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        List<WorkspaceEntity> workspaces = workspaceService.listWorkspacesForUser(userId);
        return ResponseEntity.ok(workspaces);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkspaceEntity> getWorkspace(
            @PathVariable UUID id,
            Principal principal
    ) {
        UUID userId = UUID.fromString(principal.getName());
        WorkspaceEntity workspace = workspaceService.getWorkspace(id, userId);
        return ResponseEntity.ok(workspace);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<Void> inviteMember(
            @PathVariable UUID id,
            @RequestBody @Valid InviteMemberRequest request,
            Principal principal
    ) {
        UUID inviterId = UUID.fromString(principal.getName());
        workspaceService.inviteMember(id, inviterId, request.userId(), request.role());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            Principal principal
    ) {
        UUID requesterId = UUID.fromString(principal.getName());
        workspaceService.removeMember(id, requesterId, userId);
        return ResponseEntity.noContent().build();
    }

    // DTO records
    public record CreateWorkspaceRequest(@NotBlank String name) {}
    public record InviteMemberRequest(@NotNull UUID userId, @NotBlank String role) {}
}
