package uk.jsikora.woodworksapi.workService;

import java.util.List;

public interface CabinCuttingStrategy {
    boolean supports(CabinType type);
    List<Item> generateItems(WorkRequest.CabinRequest cabinRequest);
}