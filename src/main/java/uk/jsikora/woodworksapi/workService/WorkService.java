package uk.jsikora.woodworksapi.workService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkService {

    private final List<CabinCuttingStrategy> generators;

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