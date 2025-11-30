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
        DrawerSystem drawerSystem = cabinRequest.drawerSystem();
        
        log.info("Generating drawers cabin items. System: {}", drawerSystem);

        int innerWidth = width - 2 * thickness;

        List<Item> items = new ArrayList<>();

        // Carcass
        // Boki
        items.add(new Item("[Korpus] Bok (lewy)", depth, height, thickness, 1, PLYTA_MEBLOWA));
        items.add(new Item("[Korpus] Bok (prawy)", depth, height, thickness, 1, PLYTA_MEBLOWA));

        // Spód
        items.add(new Item("[Korpus] Wieniec dolny", innerWidth, depth, thickness, 1, PLYTA_MEBLOWA));

        // Belki górne
        items.add(new Item("[Korpus] Wieniec górny (przód)", innerWidth, TOP_BEAM_HEIGHT, thickness, 1, PLYTA_MEBLOWA));
        items.add(new Item("[Korpus] Wieniec górny (tył)", innerWidth, TOP_BEAM_HEIGHT, thickness, 1, PLYTA_MEBLOWA));

        // Plecy
        items.add(new Item("[Korpus] Plecy", width - 2, height - 2, 3, 1, HDF));

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

            items.add(new Item("[Szuflada] Front " + (i + 1), width - FRONT_CLEARANCE, actualFrontHeight, thickness, 1, PLYTA_MEBLOWA));

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
            items.add(new Item("[Szuflada] Plecy " + (i + 1), drawerBoxWidth - 2 * 16, drawerBoxHeight, 16, 1, PLYTA_MEBLOWA));
            
            // Box bottom
            // Thickness 16mm
            items.add(new Item("[Szuflada] Dno " + (i + 1), drawerBoxWidth, drawerBoxDepth, 16, 1, PLYTA_MEBLOWA));
        }

        addPlinthDrawer(items, cabinRequest, width, depth, thickness);

        return ItemUtils.aggregateItems(items);
    }

    private void addPlinthDrawer(List<Item> items, WorkRequest.CabinRequest cabinRequest, int width, int depth, int thickness) {
        if (!Boolean.TRUE.equals(cabinRequest.plinthDrawer())) {
            return;
        }

        int baseboardHeight = cabinRequest.baseboardHeight() != null ? cabinRequest.baseboardHeight() : 100;
        int legDiameter = cabinRequest.legDiameter() != null ? cabinRequest.legDiameter() : 60;
        
        // Calculate dynamic drawer width (same logic as frontend)
        // Legs are flush with edge (inset = radius)
        // Space taken by one leg = diameter
        double legSpacePerSide = legDiameter;
        int availableWidth = width - (int)(2 * legSpacePerSide);
        int minClearance = 10;
        int maxPossibleWidth = availableWidth - minClearance;
        // Round down to nearest 50mm
        int drawerWidth = (int) (Math.floor(maxPossibleWidth / 50.0) * 50);

        if (drawerWidth < 300) {
            log.warn("Not enough space for plinth drawer. Width: {}, Leg: {}, Calc: {}", width, legDiameter, drawerWidth);
            return;
        }

        // Front panel
        int frontHeight = baseboardHeight - 4;
        items.add(new Item("[Szuflada cokołowa] Front", width - 4, frontHeight, thickness, 1, PLYTA_MEBLOWA));

        // Drawer Box
        int boxDepth = Math.min(400, depth - 100);
        int boxHeight = Math.min(60, frontHeight - 20);
        
        // Box front/back (internal)
        int internalFrontWidth = drawerWidth - (2 * thickness);
        items.add(new Item("[Szuflada cokołowa] Plecy", internalFrontWidth, boxHeight, thickness, 2, PLYTA_MEBLOWA));
        // Box bottom
        items.add(new Item("[Szuflada cokołowa] Dno", drawerWidth, boxDepth, thickness, 1, PLYTA_MEBLOWA));
    }
}
