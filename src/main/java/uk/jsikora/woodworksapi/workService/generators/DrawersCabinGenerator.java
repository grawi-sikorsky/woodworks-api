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
import static uk.jsikora.woodworksapi.workService.generators.ItemType.*;

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

    /**
     * Generates items for a drawers cabinet: corpus, back panel, drawer fronts and boxes.
     * Note: Drawer sides are purchased accessories and not included in cut list.
     */
    @Override
    public List<Item> generateItems(WorkRequest.CabinRequest cabinRequest) {
        log.info("Generating drawers cabin items.");
        int thickness = cabinRequest.thickness();
        int width = cabinRequest.width();
        int height = cabinRequest.height();
        int depth = cabinRequest.depth();
        int count = cabinRequest.cabinCount();
        DrawerSystem drawerSystem = cabinRequest.drawerSystem();
        
        log.info("Generating drawers cabin items. System: {}", drawerSystem);

        int innerWidth = width - 2 * thickness;

        List<Item> items = new ArrayList<>();

        // Carcass
        // Boki
        items.add(new Item("[Korpus] Bok (lewy)", depth, height, thickness, 1, PLYTA_MEBLOWA, CORPUS));
        items.add(new Item("[Korpus] Bok (prawy)", depth, height, thickness, 1, PLYTA_MEBLOWA, CORPUS));

        // Spód
        items.add(new Item("[Korpus] Wieniec dolny", innerWidth, depth, thickness, 1, PLYTA_MEBLOWA, CORPUS));

        // Belki górne
        items.add(new Item("[Korpus] Wieniec górny (przód)", innerWidth, TOP_BEAM_HEIGHT, thickness, 1, PLYTA_MEBLOWA, CORPUS));
        items.add(new Item("[Korpus] Wieniec górny (tył)", innerWidth, TOP_BEAM_HEIGHT, thickness, 1, PLYTA_MEBLOWA, CORPUS));

        // Plecy
        items.add(new Item("[Korpus] Plecy", width - 2, height - 2, 3, 1, HDF, ItemType.HDF));

        // Get drawers configuration
        List<WorkRequest.DrawerConfig> drawers = cabinRequest.drawers();
        if (drawers == null || drawers.isEmpty()) {
            log.warn("No drawers configuration provided for DRAWERS cabinet");
            return items;
        }

        // Drawers
        for (int i = 0; i < drawers.size(); i++) {
            WorkRequest.DrawerConfig drawer = drawers.get(i);
            int drawerFrontHeight = drawer.height();
            // Adjust for gaps? The heights from frontend usually sum up to total height.
            // We need to subtract gaps to get actual front height.
            // Frontend sends: (h / totalHeight) * cabinet.height.
            // So they sum up to cabinet height.
            // We should subtract gap.
            int actualFrontHeight = drawerFrontHeight - DRAWER_GAP;

            items.add(new Item("[Szuflada] Front " + (i + 1), width - FRONT_CLEARANCE, actualFrontHeight, thickness, 1, PLYTA_MEBLOWA, FRONT));

            // Drawer Box
            // Assuming standard drawer slides (e.g. Blum Tandembox or similar need specific calculations)
            // Simplified box:
            // Width = innerWidth - clearance (e.g. 13mm per side for slides -> 26mm)
            int drawerBoxWidth = innerWidth - 26;
            
            int drawerBoxDepth = drawer.depth();
            int drawerBoxHeight = Math.max(80, actualFrontHeight - 40); // Arbitrary box height logic

            // Box sides - REMOVED as per request (bought accessory)
            // items.add(new Item("Bok szuflady " + (i + 1), drawerBoxDepth, drawerBoxHeight, thickness, 2, PLYTA_MEBLOWA));
            
            // Box back (only back needed for system drawers)
            // Thickness 16mm
            items.add(new Item("[Szuflada] Plecy " + (i + 1), drawerBoxWidth - 2 * 16, drawerBoxHeight, 16, 1, PLYTA_MEBLOWA, DRAWER_BOX));
            
            // Box bottom
            // Thickness 16mm
            items.add(new Item("[Szuflada] Dno " + (i + 1), drawerBoxWidth, drawerBoxDepth, 16, 1, PLYTA_MEBLOWA, DRAWER_BOTTOM));
        }

        ItemUtils.addPlinthItems(items, cabinRequest, width, depth, thickness);

        return ItemUtils.aggregateItems(items);
    }
}
