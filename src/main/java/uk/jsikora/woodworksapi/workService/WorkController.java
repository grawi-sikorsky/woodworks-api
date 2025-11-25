package uk.jsikora.woodworksapi.workService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import uk.jsikora.woodworksapi.user.UserService;

@RestController
@RequiredArgsConstructor
public class WorkController {

    private final WorkService workService;
    private final UserService userService;

    @PostMapping("/cut-list/generate")
    public ResponseEntity<WorkResponse> generateItems(@RequestBody WorkRequest request, Authentication authentication) {
        Long userId = getUserId(authentication);
        userService.incrementGenerationCount(userId);
        return ResponseEntity.ok(workService.generateWorkResponse(request));
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
