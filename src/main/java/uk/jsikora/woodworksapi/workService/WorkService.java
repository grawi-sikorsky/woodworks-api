package uk.jsikora.woodworksapi.workService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkService {

    private final List<CabinCuttingStrategy> generators;

    public WorkResponse generateWorkResponse(WorkRequest request) {
        List<Item> allItems = request.cabins().stream()
                                     .flatMap(cabin -> generators.stream()
                                                                 .filter(g -> g.supports(cabin.cabinType()))
                                                                 .findFirst()
                                                                 .orElseThrow(() -> new IllegalArgumentException("Nieobsługiwany typ: " + cabin.cabinType()))
                                                                 .generateItems(cabin).stream())
                                     .toList();

        WorkResponse response = new WorkResponse();
        response.setStatus("SUCCESS");
        response.setItemList(allItems);
        return response;
    }
}