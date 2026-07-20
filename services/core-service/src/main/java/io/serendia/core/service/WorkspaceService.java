package io.serendia.core.service;

import io.serendia.core.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceService {

    private final WorkspaceRepository       workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    @Transactional
    public WorkspaceEntity createWorkspace(UUID ownerId, String name) {
        WorkspaceEntity.WorkspaceEntityBuilder builder = WorkspaceEntity.builder()
                .ownerId(ownerId)
                .name(name)
                .plan("FREE")
                .settings(Map.of());

        if ("Serendia Agency".equalsIgnoreCase(name)) {
            builder.id(UUID.fromString("1a938634-fc56-3b2d-965c-3f2603847522"));
        }

        WorkspaceEntity workspace = builder.build();
        WorkspaceEntity saved = workspaceRepository.save(workspace);

        // Auto-add owner as ADMIN member
        WorkspaceMemberEntity member = WorkspaceMemberEntity.builder()
                .workspaceId(saved.getId())
                .userId(ownerId)
                .role("ADMIN")
                .build();
        workspaceMemberRepository.save(member);

        log.info("Created workspace {} for owner {}", saved.getId(), ownerId);
        return saved;
    }

    public List<WorkspaceEntity> listWorkspacesForUser(UUID userId) {
        return workspaceRepository.findAllForUser(userId);
    }

    public WorkspaceEntity getWorkspace(UUID workspaceId, UUID userId) {
        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        if (!workspace.getOwnerId().equals(userId) &&
                !workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to workspace");
        }
        return workspace;
    }

    @Transactional
    public void inviteMember(UUID workspaceId, UUID inviterId, UUID targetUserId, String role) {
        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        // Validate requester is Owner or Admin
        boolean isOwner = workspace.getOwnerId().equals(inviterId);
        boolean isAdmin = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, inviterId)
                .map(m -> "ADMIN".equalsIgnoreCase(m.getRole()))
                .orElse(false);

        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only workspace owners or admins can invite members");
        }

        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, targetUserId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a member of this workspace");
        }

        WorkspaceMemberEntity member = WorkspaceMemberEntity.builder()
                .workspaceId(workspaceId)
                .userId(targetUserId)
                .role(role.toUpperCase())
                .build();
        workspaceMemberRepository.save(member);

        log.info("User {} invited target user {} to workspace {} as {}", inviterId, targetUserId, workspaceId, role);
        // TODO: Send invite email via AWS SES (Milestone 1 infrastructure scope placeholder)
    }

    @Transactional
    public void removeMember(UUID workspaceId, UUID requesterId, UUID targetUserId) {
        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        if (workspace.getOwnerId().equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot remove the owner from their own workspace");
        }

        // Validate requester is Owner or Admin
        boolean isOwner = workspace.getOwnerId().equals(requesterId);
        boolean isAdmin = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, requesterId)
                .map(m -> "ADMIN".equalsIgnoreCase(m.getRole()))
                .orElse(false);

        if (!isOwner && !isAdmin && !requesterId.equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Permission denied to remove member");
        }

        WorkspaceMemberEntity member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found in workspace"));

        workspaceMemberRepository.delete(member);
        log.info("Member {} removed from workspace {} by requester {}", targetUserId, workspaceId, requesterId);
    }
}
