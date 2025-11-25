package uk.jsikora.woodworksapi.workService;

import java.util.List;

public record WorkRequest(int workType, List<CabinRequest> cabins) {

    public record CabinRequest(CabinType cabinType,
                               int width,
                               int height,
                               int depth,
                               int thickness,
                               int cabinCount,
                               List<Integer> drawerHeights,
                               Integer doorCount,
                               Boolean hasOvenDrawer,
                               Integer baseboardHeight,
                               String cabinetName,
                               ColorConfig colors) {
    }

    public record ColorConfig(String corpusColor,
                              String frontColor,
                              String drawerInteriorColor,
                              String hdfColor) {
    }
}
