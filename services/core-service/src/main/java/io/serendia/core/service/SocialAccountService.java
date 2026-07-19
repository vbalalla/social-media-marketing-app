package io.serendia.core.service;

import io.serendia.core.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocialAccountService {

    private final SocialAccountRepository    socialAccountRepository;
    private final WorkspaceRepository         workspaceRepository;
    private final WorkspaceMemberRepository  workspaceMemberRepository;

    public List<SocialAccountEntity> listAccounts(UUID workspaceId, UUID userId) {
        // Membership check
        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        if (!workspace.getOwnerId().equals(userId) &&
                !workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to workspace");
        }

        return socialAccountRepository.findByWorkspaceIdAndIsActiveTrue(workspaceId);
    }

    @Transactional
    public void disconnectAccount(UUID socialAccountId, UUID userId) {
        SocialAccountEntity account = socialAccountRepository.findById(socialAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Social account not found"));

        WorkspaceEntity workspace = workspaceRepository.findById(account.getWorkspaceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        boolean isOwner = workspace.getOwnerId().equals(userId);
        boolean isAdmin = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspace.getId(), userId)
                .map(m -> "ADMIN".equalsIgnoreCase(m.getRole()))
                .orElse(false);

        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only workspace owners or admins can disconnect accounts");
        }

        account.setActive(false);
        socialAccountRepository.save(account);
        log.info("Disconnected social account {} from workspace {}", socialAccountId, workspace.getId());
    }
}
