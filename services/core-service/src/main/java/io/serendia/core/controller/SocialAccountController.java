package io.serendia.core.controller;

import io.serendia.core.domain.SocialAccountEntity;
import io.serendia.core.service.SocialAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SocialAccountController {

    private final SocialAccountService socialAccountService;

    @GetMapping("/workspaces/{workspaceId}/social-accounts")
    public ResponseEntity<List<SocialAccountEntity>> listSocialAccounts(
            @PathVariable UUID workspaceId,
            Principal principal
    ) {
        UUID userId = UUID.fromString(principal.getName());
        List<SocialAccountEntity> accounts = socialAccountService.listAccounts(workspaceId, userId);
        return ResponseEntity.ok(accounts);
    }

    @DeleteMapping("/social-accounts/{id}")
    public ResponseEntity<Void> disconnectSocialAccount(
            @PathVariable UUID id,
            Principal principal
    ) {
        UUID userId = UUID.fromString(principal.getName());
        socialAccountService.disconnectAccount(id, userId);
        return ResponseEntity.noContent().build();
    }
}
