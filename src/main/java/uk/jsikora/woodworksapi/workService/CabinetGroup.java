package uk.jsikora.woodworksapi.workService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CabinetGroup {
    private String cabinetName;
    private CabinType cabinetType;
    private int cabinetCount;
    private List<Item> items;
}
