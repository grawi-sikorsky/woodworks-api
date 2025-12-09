package uk.jsikora.woodworksapi.workService.generators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.jsikora.woodworksapi.workService.CabinCuttingStrategy;
import uk.jsikora.woodworksapi.workService.CabinType;
import uk.jsikora.woodworksapi.workService.Item;
import uk.jsikora.woodworksapi.workService.ItemUtils;
import uk.jsikora.woodworksapi.workService.WorkRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static uk.jsikora.woodworksapi.workService.MaterialType.HDF;
import static uk.jsikora.woodworksapi.workService.MaterialType.PLYTA_MEBLOWA;
import static uk.jsikora.woodworksapi.workService.generators.ItemType.*;

@Slf4j
@Component
public class OvenCabinGenerator implements CabinCuttingStrategy {

    private static final int FRONT_CLEARANCE = 4;
    private static final int OVEN_HEIGHT = 595; // Standard oven height
    private static final int DRAWER_GAP = 3;

    /**
     * Determines if this generator supports the given cabinet type.
     * 
     * @param cabinType the type of cabinet to check
     * @return true if the cabinet type is OVEN, false otherwise
     */
    @Override
    public boolean supports(CabinType cabinType) {
        return cabinType == CabinType.OVEN;
    }

    /**
     * Generates the list of items (parts) required for an oven cabinet.
     * Creates corpus elements (sides, beams), shelf for oven placement,
     * optional drawer with front and box components, back panel, and plinth items.
     * 
     * @param cabinRequest the cabinet configuration containing dimensions, drawer settings, and other parameters
     * @return aggregated list of items with their dimensions, materials, and quantities
     */
    @Override
    public List<Item> generateItems(WorkRequest.CabinRequest cabinRequest) {
        log.info("Generating oven cabin items.");
        int thickness = cabinRequest.thickness();
        int width = cabinRequest.width();
        int height = cabinRequest.height();
        int depth = cabinRequest.depth();
        int count = cabinRequest.cabinCount();
        boolean hasDrawer = Boolean.TRUE.equals(cabinRequest.hasOvenDrawer());

        int innerWidth = width - 2 * thickness;

        List<Item> items = new ArrayList<>();

        // Carcass
        items.add(new Item("[Korpus] Bok (lewy)", depth, height, thickness, 1, PLYTA_MEBLOWA, CORPUS));
        items.add(new Item("[Korpus] Bok (prawy)", depth, height, thickness, 1, PLYTA_MEBLOWA, CORPUS));
        items.add(new Item("[Korpus] Wieniec", innerWidth, depth, thickness, 1, PLYTA_MEBLOWA, CORPUS));
        
        // Top beam? Usually oven cabinets have a top panel or beam.
        items.add(new Item("[Korpus] Wieniec górny", innerWidth, depth, thickness, 1, PLYTA_MEBLOWA, CORPUS));

        // Shelf for Oven
        // Position depends on drawer height.
        // If drawer, shelf is above drawer.
        // If no drawer, shelf is at bottom? No, oven needs a shelf to sit on.
        // Let's assume shelf is always there.
        // If drawer, shelf is at drawer height.
        // If no drawer, maybe just a filler panel?
        
        int ovenShelfY = 0;
        if (hasDrawer) {
            // Drawer height approx 20% of total height or remaining space?
            // Frontend says 20%.
            int drawerHeight = (int) (height * 0.2);
            ovenShelfY = drawerHeight;
            
            items.add(new Item("[Korpus] Półka pod piekarnik", innerWidth, depth, thickness, 1, PLYTA_MEBLOWA, SHELF));
            
            // Drawer Front
            items.add(new Item("[Szuflada] Front", width - FRONT_CLEARANCE, drawerHeight - DRAWER_GAP, thickness, 1, PLYTA_MEBLOWA, FRONT));
            
            // Drawer Box (simplified)
            int drawerBoxHeight = Math.max(80, drawerHeight - 40);
            items.add(new Item("[Szuflada] Plecy", innerWidth - 26 - 2 * thickness, drawerBoxHeight, thickness, 2, PLYTA_MEBLOWA, DRAWER_BOX));
            items.add(new Item("[Szuflada] Dno", innerWidth - 26, depth - 10, thickness, 1, PLYTA_MEBLOWA, DRAWER_BOTTOM));
        } else {
            // Just a shelf at bottom or filler?
            // Usually a filler panel at bottom if no drawer.
            items.add(new Item("[Korpus] Półka pod piekarnik", innerWidth, depth, thickness, 1, PLYTA_MEBLOWA, SHELF));
            items.add(new Item("[Korpus] Blenda dolna", width - FRONT_CLEARANCE, 100, thickness, 1, PLYTA_MEBLOWA, FRONT)); // Arbitrary filler
        }

        // Plecy (usually open behind oven for ventilation, but maybe small piece at bottom?)
        // Let's add full back for now, user can cut.
        items.add(new Item("[Korpus] Plecy", width - 2, height - 2, 3, 1, HDF, ItemType.HDF));

        ItemUtils.addPlinthItems(items, cabinRequest, width, depth, thickness);

        return ItemUtils.aggregateItems(items);
    }
}
