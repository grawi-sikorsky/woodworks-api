package uk.jsikora.woodworksapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uk.jsikora.woodworksapi.dto.KitchenDraftDto;
import uk.jsikora.woodworksapi.dto.KitchenDraftSummaryDto;
import uk.jsikora.woodworksapi.dto.SaveKitchenDraftRequest;
import uk.jsikora.woodworksapi.service.KitchenDraftService;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing kitchen project drafts.
 * Provides CRUD operations for user's kitchen design projects.
 */
@RestController
@RequestMapping("/api/kitchen-drafts")
@RequiredArgsConstructor
public class KitchenDraftController {

    private final KitchenDraftService service;
    private final uk.jsikora.woodworksapi.user.UserService userService;

    /** Creates a new kitchen draft. */
    @PostMapping
    public ResponseEntity<KitchenDraftDto> saveDraft(
            @RequestBody SaveKitchenDraftRequest request,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        KitchenDraftDto draft = service.saveDraft(userId, request);
        return ResponseEntity.ok(draft);
    }

    /** Updates an existing kitchen draft. */
    @PutMapping("/{uuid}")
    public ResponseEntity<KitchenDraftDto> updateDraft(
            @PathVariable UUID uuid,
            @RequestBody SaveKitchenDraftRequest request,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        KitchenDraftDto draft = service.updateDraft(userId, uuid, request);
        return ResponseEntity.ok(draft);
    }

    /** Retrieves all draft summaries for the authenticated user. */
    @GetMapping
    public ResponseEntity<List<KitchenDraftSummaryDto>> getUserDrafts(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<KitchenDraftSummaryDto> drafts = service.getUserDraftSummaries(userId);
        return ResponseEntity.ok(drafts);
    }

    /** Renames an existing kitchen draft. */
    @PatchMapping("/{uuid}/name")
    public ResponseEntity<Void> renameDraft(
            @PathVariable UUID uuid,
            @RequestBody String newName,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        service.renameDraft(userId, uuid, newName);
        return ResponseEntity.ok().build();
    }

    /** Retrieves a specific kitchen draft by UUID. */
    @GetMapping("/{uuid}")
    public ResponseEntity<KitchenDraftDto> getDraft(
            @PathVariable UUID uuid,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        KitchenDraftDto draft = service.getDraft(userId, uuid);
        return ResponseEntity.ok(draft);
    }

    /** Deletes a kitchen draft. */
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteDraft(
            @PathVariable UUID uuid,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        service.deleteDraft(userId, uuid);
        return ResponseEntity.noContent().build();
    }

    /** Extracts user ID from authentication context. */
    private Long getUserId(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof uk.jsikora.woodworksapi.user.BaseUser) {
            return ((uk.jsikora.woodworksapi.user.BaseUser) principal).getId();
        } else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            String email = ((org.springframework.security.oauth2.core.user.OAuth2User) principal).getAttribute("email");
            return userService.findByEmail(email)
                    .map(uk.jsikora.woodworksapi.user.BaseUser::getId)
                    .orElseThrow(() -> new RuntimeException("User not found in database: " + email));
        } else {
            throw new RuntimeException("Unsupported authentication type: " + principal.getClass().getName());
        }
    }
}
