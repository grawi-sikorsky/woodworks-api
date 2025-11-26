package uk.jsikora.woodworksapi.workService.generators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.jsikora.woodworksapi.workService.CabinCuttingStrategy;
import uk.jsikora.woodworksapi.workService.CabinType;
import uk.jsikora.woodworksapi.workService.DrawerSystem;
import uk.jsikora.woodworksapi.workService.Item;
import uk.jsikora.woodworksapi.workService.ItemUtils;
import uk.jsikora.woodworksapi.workService.WorkRequest;

import java.util.ArrayList;
import java.util.List;

import static uk.jsikora.woodworksapi.workService.MaterialType.HDF;
import static uk.jsikora.woodworksapi.workService.MaterialType.PLYTA_MEBLOWA;

@Slf4j
@Component
public class DrawersCabinGenerator implements CabinCuttingStrategy {

    private static final int FRONT_CLEARANCE = 4;
    private static final int TOP_BEAM_HEIGHT = 100;
    private static final int DRAWER_GAP = 3;

    @Override
    public boolean supports(CabinType cabinType) {
        return cabinType == CabinType.DRAWERS;
    }

    @Override
    public List<Item> generateItems(WorkRequest.CabinRequest cabinRequest) {
        log.info("Generating drawers cabin items.");
        int thickness = cabinRequest.thickness();
        int width = cabinRequest.width();
        int height = cabinRequest.height();
        int depth = cabinRequest.depth();
        int count = cabinRequest.cabinCount();
        List<Integer> drawerHeights = cabinRequest.drawerHeights();
        DrawerSystem drawerSystem = cabinRequest.drawerSystem();
        
        log.info("Generating drawers cabin items. System: {}", drawerSystem);

        int innerWidth = width - 2 * thickness;

        List<Item> items = new ArrayList<>();

        // Carcass
        // Boki
        items.add(new Item("Bok (lewy)", depth, height, thickness, 1, PLYTA_MEBLOWA));
        items.add(new Item("Bok (prawy)", depth, height, thickness, 1, PLYTA_MEBLOWA));

        // Spód
        items.add(new Item("Spód", innerWidth, depth, thickness, 1, PLYTA_MEBLOWA));

        // Belki górne
        items.add(new Item("Belka górna (przód)", innerWidth, TOP_BEAM_HEIGHT, thickness, 1, PLYTA_MEBLOWA));
        items.add(new Item("Belka górna (tył)", innerWidth, TOP_BEAM_HEIGHT, thickness, 1, PLYTA_MEBLOWA));

        // Plecy
        items.add(new Item("Plecy", width - 2, height - 2, 3, 1, HDF));

        // Drawers
        if (drawerHeights != null) {
            for (int i = 0; i < drawerHeights.size(); i++) {
                int drawerFrontHeight = drawerHeights.get(i);
                // Adjust for gaps? The heights from frontend usually sum up to total height.
                // We need to subtract gaps to get actual front height.
                // Or assume frontend sends exact front heights?
                // Frontend sends: (h / totalHeight) * cabinet.height.
                // So they sum up to cabinet height.
                // We should subtract gap.
                int actualFrontHeight = drawerFrontHeight - DRAWER_GAP;

                items.add(new Item("Front szuflady " + (i + 1), width - FRONT_CLEARANCE, actualFrontHeight, thickness, 1, PLYTA_MEBLOWA));

                // Drawer Box
                // Assuming standard drawer slides (e.g. Blum Tandembox or similar need specific calculations)
                // Simplified box:
                // Width = innerWidth - clearance (e.g. 13mm per side for slides -> 26mm)
                int drawerBoxWidth = innerWidth - 26;
                int drawerBoxDepth = depth - 10; // Clearance at back
                int drawerBoxHeight = Math.max(80, actualFrontHeight - 40); // Arbitrary box height logic

                // Box sides
                items.add(new Item("Bok szuflady " + (i + 1), drawerBoxDepth, drawerBoxHeight, thickness, 2, PLYTA_MEBLOWA));
                // Box front/back
                items.add(new Item("Przód/Tył szuflady " + (i + 1), drawerBoxWidth - 2 * thickness, drawerBoxHeight, thickness, 2, PLYTA_MEBLOWA));
                // Box bottom
                items.add(new Item("Dno szuflady " + (i + 1), drawerBoxWidth, drawerBoxDepth, 3, 1, HDF));
            }
        }

        return ItemUtils.aggregateItems(items);
    }
}
