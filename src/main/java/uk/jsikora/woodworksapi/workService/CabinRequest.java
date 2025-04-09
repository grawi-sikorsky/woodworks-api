package uk.jsikora.woodworksapi.workService;

public record CabinRequest(CabinType cabinType,
                           int width,
                           int height,
                           int depth,
                           int cabinCount) {
}
