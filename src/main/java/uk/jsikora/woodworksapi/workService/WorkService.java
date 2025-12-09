package uk.jsikora.woodworksapi.workService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for processing work requests and generating cut lists.
 * Coordinates with cabinet generators to produce item lists for each cabinet type.
 */
@Service
@RequiredArgsConstructor
public class WorkService {

    private final List<CabinCuttingStrategy> generators;

    /**
     * Processes a work request and generates a complete work response with cut lists.
     * For each cabinet in the request, finds the appropriate generator strategy,
     * generates items, and groups them by cabinet.
     * 
     * @param request the work request containing cabinet configurations
     * @return WorkResponse containing grouped cabinet items and status
     * @throws IllegalArgumentException if an unsupported cabinet type is encountered
     */
    public WorkResponse generateWorkResponse(WorkRequest request) {
        List<CabinetGroup> cabinetGroups = request.cabins().stream()
                .map(cabin -> {
                    // Find the appropriate generator
                    List<Item> items = generators.stream()
                            .filter(g -> g.supports(cabin.cabinType()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Nieobsługiwany typ: " + cabin.cabinType()))
                            .generateItems(cabin);

                    // Create cabinet group
                    String cabinetName = cabin.cabinetName() != null && !cabin.cabinetName().isEmpty()
                            ? cabin.cabinetName()
                            : cabin.cabinType().toString();

                    return new CabinetGroup(cabinetName, cabin.cabinType(), cabin.cabinCount(), items);
                })
                .toList();

        WorkResponse response = new WorkResponse();
        response.setStatus("SUCCESS");
        response.setCabinetGroups(cabinetGroups);
        return response;
    }
}