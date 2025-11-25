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

@Slf4j
@Component
public class OvenCabinGenerator implements CabinCuttingStrategy {

    private static final int FRONT_CLEARANCE = 4;
    private static final int OVEN_HEIGHT = 595; // Standard oven height
    private static final int DRAWER_GAP = 3;

    @Override
    public boolean supports(CabinType cabinType) {
        return cabinType == CabinType.OVEN;
    }

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
        items.add(new Item("Bok (lewy)", depth, height, thickness, 1, PLYTA_MEBLOWA));
        items.add(new Item("Bok (prawy)", depth, height, thickness, 1, PLYTA_MEBLOWA));
        items.add(new Item("Spód", innerWidth, depth, thickness, 1, PLYTA_MEBLOWA));
        
        // Top beam? Usually oven cabinets have a top panel or beam.
        items.add(new Item("Wieniec górny", innerWidth, depth, thickness, 1, PLYTA_MEBLOWA));

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
            
            items.add(new Item("Półka pod piekarnik", innerWidth, depth, thickness, 1, PLYTA_MEBLOWA));
            
            // Drawer Front
            items.add(new Item("Front szuflady", width - FRONT_CLEARANCE, drawerHeight - DRAWER_GAP, thickness, 1, PLYTA_MEBLOWA));
            
            // Drawer Box (simplified)
            int drawerBoxHeight = Math.max(80, drawerHeight - 40);
            items.add(new Item("Bok szuflady", depth - 10, drawerBoxHeight, thickness, 2, PLYTA_MEBLOWA));
            items.add(new Item("Przód/Tył szuflady", innerWidth - 26 - 2 * thickness, drawerBoxHeight, thickness, 2, PLYTA_MEBLOWA));
            items.add(new Item("Dno szuflady", innerWidth - 26, depth - 10, 3, 1, HDF));
        } else {
            // Just a shelf at bottom or filler?
            // Usually a filler panel at bottom if no drawer.
            items.add(new Item("Półka pod piekarnik", innerWidth, depth, thickness, 1, PLYTA_MEBLOWA));
            items.add(new Item("Blend dolna", width - FRONT_CLEARANCE, 100, thickness, 1, PLYTA_MEBLOWA)); // Arbitrary filler
        }

        // Plecy (usually open behind oven for ventilation, but maybe small piece at bottom?)
        // Let's add full back for now, user can cut.
        items.add(new Item("Plecy", width - 2, height - 2, 3, 1, HDF));

        return ItemUtils.aggregateItems(items);
    }
}
