package uk.jsikora.woodworksapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uk.jsikora.woodworksapi.dto.KitchenDraftDto;
import uk.jsikora.woodworksapi.dto.SaveKitchenDraftRequest;
import uk.jsikora.woodworksapi.service.KitchenDraftService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/kitchen-drafts")
@RequiredArgsConstructor
public class KitchenDraftController {

    private final KitchenDraftService service;
    private final uk.jsikora.woodworksapi.user.UserService userService;

    @PostMapping
    public ResponseEntity<KitchenDraftDto> saveDraft(
            @RequestBody SaveKitchenDraftRequest request,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        KitchenDraftDto draft = service.saveDraft(userId, request);
        return ResponseEntity.ok(draft);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<KitchenDraftDto> updateDraft(
            @PathVariable UUID uuid,
            @RequestBody SaveKitchenDraftRequest request,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        KitchenDraftDto draft = service.updateDraft(userId, uuid, request);
        return ResponseEntity.ok(draft);
    }

    @GetMapping
    public ResponseEntity<List<KitchenDraftDto>> getUserDrafts(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<KitchenDraftDto> drafts = service.getUserDrafts(userId);
        return ResponseEntity.ok(drafts);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<KitchenDraftDto> getDraft(
            @PathVariable UUID uuid,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        KitchenDraftDto draft = service.getDraft(userId, uuid);
        return ResponseEntity.ok(draft);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteDraft(
            @PathVariable UUID uuid,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        service.deleteDraft(userId, uuid);
        return ResponseEntity.noContent().build();
    }

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
