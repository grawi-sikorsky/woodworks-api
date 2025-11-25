package uk.jsikora.woodworksapi.workService;

import lombok.Data;

import java.util.List;

@Data
public class WorkResponse {
    private String status;
    private List<CabinetGroup> cabinetGroups;
}
