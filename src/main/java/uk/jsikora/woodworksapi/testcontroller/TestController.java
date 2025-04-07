package uk.jsikora.woodworksapi.testcontroller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "To jest publiczny endpoint.";
    }

    @GetMapping("/secured")
    public String securedEndpoint() {
        return "To jest prywatny endpojnt";
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal OAuth2User user) {
        return user.getAttributes();
    }
}
