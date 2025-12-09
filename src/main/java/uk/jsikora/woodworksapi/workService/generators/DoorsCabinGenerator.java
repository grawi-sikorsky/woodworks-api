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
public class DoorsCabinGenerator implements CabinCuttingStrategy {

    private static final int FRONT_CLEARANCE = 4;
    private static final int TOP_BEAM_HEIGHT = 100;

    /**
     * Determines if this generator supports the given cabinet type.
     * 
     * @param cabinType the type of cabinet to check
     * @return true if the cabinet type is DOORS, false otherwise
     */
    @Override
    public boolean supports(CabinType cabinType) {
        return cabinType == CabinType.DOORS;
    }

    /**
     * Generates the list of items (parts) required for a doors cabinet.
     * Creates corpus elements (sides, beams, bottom), back panel, shelf,
     * door fronts (single or double based on configuration), and plinth items.
     * 
     * @param cabinRequest the cabinet configuration containing dimensions, door count, and other parameters
     * @return aggregated list of items with their dimensions, materials, and quantities
     */
    @Override
    public List<Item> generateItems(WorkRequest.CabinRequest cabinRequest) {
        log.info("Generating doors cabin items.");
        int thickness = cabinRequest.thickness();
        int width = cabinRequest.width();
        int height = cabinRequest.height();
        int depth = cabinRequest.depth();
        int count = cabinRequest.cabinCount();
        int baseboardHeight = cabinRequest.baseboardHeight() != null ? cabinRequest.baseboardHeight() : 0;
        int doorCount = cabinRequest.doorCount() != null ? cabinRequest.doorCount() : 1;

        // Adjust height for baseboard if it's part of the cabinet structure or just legs?
        // Usually baseboard is separate or legs. Assuming height is total height including legs/baseboard?
        // Or is height the carcass height?
        // Frontend preview shows baseboard as separate visual element below cabinet.
        // Let's assume 'height' is the carcass height for now, or total height?
        // In preview: [style.--h.px]="cabinet.height * scale"
        // And baseboard is separate.
        // So 'height' is the cabinet box height.
        
        int innerWidth = width - 2 * thickness;

        List<Item> items = new ArrayList<>();

        // Boki
        // Boki
        items.add(new Item("[Korpus] Bok (lewy)", depth, height, thickness, 1, PLYTA_MEBLOWA, CORPUS));
        items.add(new Item("[Korpus] Bok (prawy)", depth, height, thickness, 1, PLYTA_MEBLOWA, CORPUS));

        // Spód
        items.add(new Item("[Korpus] Wieniec", innerWidth, depth, thickness, 1, PLYTA_MEBLOWA, CORPUS));

        // Belka górna (front and back or just one?)
        // Standard kitchen cabinet has 2 top beams usually.
        items.add(new Item("[Korpus] Wieniec górny (przód)", innerWidth, TOP_BEAM_HEIGHT, thickness, 1, PLYTA_MEBLOWA, CORPUS));
        items.add(new Item("[Korpus] Wieniec górny (tył)", innerWidth, TOP_BEAM_HEIGHT, thickness, 1, PLYTA_MEBLOWA, CORPUS));

        // Plecy (HDF)
        // Usually inserted into grooves or nailed on back.
        // Assuming nailed on back for simplicity or standard groove size.
        // Let's assume full size minus some offset if groove, or full size - 2mm.
        // Old generator: width, height, 3, 1, HDF.
        items.add(new Item("[Korpus] Plecy", width - 2, height - 2, 3, 1, HDF, ItemType.HDF));

        // Półka (optional, but standard usually has 1)
        items.add(new Item("[Korpus] Półka", innerWidth - 2, depth - 20, thickness, 1, PLYTA_MEBLOWA, SHELF));

        // Fronts
        int frontHeight = height - FRONT_CLEARANCE;
        if (doorCount == 2) {
            int singleDoorWidth = (width - FRONT_CLEARANCE * 2) / 2; // Gap in middle too?
            // Usually gap in middle is small.
            // Let's say (width - 4 - 2) / 2 ?
            // Let's simplify: (width - 4) / 2
            items.add(new Item("[Korpus] Front (lewy)", singleDoorWidth, frontHeight, thickness, 1, PLYTA_MEBLOWA, FRONT));
            items.add(new Item("[Korpus] Front (prawy)", singleDoorWidth, frontHeight, thickness, 1, PLYTA_MEBLOWA, FRONT));
        } else {
            items.add(new Item("[Korpus] Front", width - FRONT_CLEARANCE, frontHeight, thickness, 1, PLYTA_MEBLOWA, FRONT));
        }

        ItemUtils.addPlinthItems(items, cabinRequest, width, depth, thickness);

        return ItemUtils.aggregateItems(items);
    }
}
