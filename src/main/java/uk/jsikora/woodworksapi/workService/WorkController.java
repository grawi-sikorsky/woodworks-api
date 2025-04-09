package uk.jsikora.woodworksapi.workService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WorkController {

    private final WorkService workService;

    @PostMapping("/items")
    public ResponseEntity<WorkResponse> generateItems(@RequestBody WorkRequest request) {
        return ResponseEntity.ok(workService.generateWorkResponse(request));
    }
}
