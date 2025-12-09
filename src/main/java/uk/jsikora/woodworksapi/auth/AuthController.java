package uk.jsikora.woodworksapi.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.jsikora.woodworksapi.user.BaseUser;
import uk.jsikora.woodworksapi.user.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for authentication-related endpoints.
 * Handles user authentication, token validation, and logout operations.
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthController(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves the current authenticated user's information.
     * Validates the JWT token and returns user details.
     * 
     * @param authHeader the Authorization header containing the Bearer token
     * @return ResponseEntity with user information or error message
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        try {
            String email = jwtService.getEmailFromToken(token);
            Optional<BaseUser> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                     .body("BaseUser not found");
            }

            BaseUser baseUser = userOpt.get();

            Map<String, Object> response = new HashMap<>();
            response.put("id", baseUser.getId());
            response.put("email", baseUser.getEmail());
            response.put("name", baseUser.getName());
            response.put("imageUrl", baseUser.getImageUrl() != null ? baseUser.getImageUrl() : "");
            response.put("provider", baseUser.getProvider());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Exception", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Invalid token");
        }
    }

    /**
     * Logs out the current user.
     * Clears the security context, invalidates the session, and removes all cookies.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @return ResponseEntity with success message
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // Clear Spring Security context
        SecurityContextHolder.clearContext();
        
        // Invalidate HTTP session if it exists
        if (request.getSession(false) != null) {
            request.getSession().invalidate();
        }
        
        // Clear all cookies
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
        
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}