package uk.jsikora.woodworksapi.workService;

import java.util.List;

public record WorkRequest(int workType, List<CabinRequest> cabins) {

    public record CabinRequest(CabinType cabinType,
                               int width,
                               int height,
                               int depth,
                               int thickness,
                               int cabinCount) {
    }
}
