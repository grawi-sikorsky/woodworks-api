package uk.jsikora.woodworksapi.testcontroller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String testMethod() {
        return "Testy";
    }
}
