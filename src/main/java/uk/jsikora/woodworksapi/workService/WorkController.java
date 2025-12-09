package uk.jsikora.woodworksapi.workService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import uk.jsikora.woodworksapi.user.UserService;

/**
 * REST controller for handling cut list generation requests.
 * Provides endpoints for generating cabinet cut lists based on user configurations.
 */
@RestController
@RequiredArgsConstructor
public class WorkController {

    private final WorkService workService;
    private final UserService userService;

    /**
     * Generates a cut list for the provided cabinet configurations.
     * Increments the user's generation count and delegates to WorkService for processing.
     * 
     * @param request the work request containing cabinet configurations
     * @param authentication the current user's authentication context
     * @return ResponseEntity containing the generated work response with cut list items
     */
    @PostMapping("/cut-list/generate")
    public ResponseEntity<WorkResponse> generateItems(@RequestBody WorkRequest request, Authentication authentication) {
        Long userId = getUserId(authentication);
        userService.incrementGenerationCount(userId);
        return ResponseEntity.ok(workService.generateWorkResponse(request));
    }

    /**
     * Extracts the user ID from the authentication context.
     * Supports both BaseUser and OAuth2User principal types.
     * 
     * @param authentication the authentication context
     * @return the user's ID
     * @throws RuntimeException if user is not authenticated, not found, or authentication type is unsupported
     */
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
