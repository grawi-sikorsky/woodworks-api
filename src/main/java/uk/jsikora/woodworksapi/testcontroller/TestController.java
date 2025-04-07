package uk.jsikora.woodworksapi.testcontroller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "To jest publiczny endpoint.";
    }

    @GetMapping("/secure")
    public String secureEndpoint() {
        return "To jest prywatny endpojnt";
    }
}
