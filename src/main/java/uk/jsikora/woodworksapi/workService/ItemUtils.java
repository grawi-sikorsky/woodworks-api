package uk.jsikora.woodworksapi.workService;

import java.util.List;
import java.util.stream.Collectors;

import uk.jsikora.woodworksapi.workService.generators.ItemType;

public class ItemUtils {

    /**
     * Aggregates items by grouping identical items and summing their counts.
     * Items are considered identical if they have the same name, dimensions (width, height, thickness),
     * material type, and item type.
     * 
     * @param items the list of items to aggregate
     * @return a new list of items with aggregated counts for identical items
     */
    public static List<Item> aggregateItems(List<Item> items) {
        return items.stream()
                    .collect(Collectors.groupingBy(item -> new ItemKey(item.name(), item.width(), item.height(), item.thickness(), item.material(), item.type()),
                                                   Collectors.summingInt(Item::count)))
                    .entrySet()
                    .stream()
                    .map(entry -> new Item(entry.getKey()
                                                .name(),
                                           entry.getKey()
                                                .width(),
                                           entry.getKey()
                                                .height(),
                                           entry.getKey()
                                                .thickness(),
                                           entry.getValue(),
                                           entry.getKey()
                                                .material(),
                                           entry.getKey()
                                                .type()))
                    .toList();
    }

    private record ItemKey(String name, int width, int height, int thickness, MaterialType material, ItemType type) {}

    /**
     * Adds plinth-related items to the items list based on cabinet configuration.
     * Generates either a standard plinth front panel or a complete plinth drawer assembly
     * (front, back, bottom) depending on the plinthDrawer setting and available space.
     * If plinth drawer is requested but insufficient width is available (< 300mm),
     * falls back to a standard plinth.
     * 
     * @param items the list to which plinth items will be added
     * @param cabinRequest the cabinet configuration containing plinth settings
     * @param width the cabinet width in mm
     * @param depth the cabinet depth in mm
     * @param thickness the board thickness in mm
     */
    public static void addPlinthItems(List<Item> items, WorkRequest.CabinRequest cabinRequest, int width, int depth, int thickness) {
        int baseboardHeight = cabinRequest.baseboardHeight() != null ? cabinRequest.baseboardHeight() : 0;
        
        if (baseboardHeight <= 0) {
            return;
        }

        if (Boolean.TRUE.equals(cabinRequest.plinthDrawer())) {
            int legDiameter = cabinRequest.legDiameter() != null ? cabinRequest.legDiameter() : 60;
            
            // Calculate dynamic drawer width
            double legSpacePerSide = legDiameter;
            int availableWidth = width - (int)(2 * legSpacePerSide);
            int minClearance = 10;
            int maxPossibleWidth = availableWidth - minClearance;
            // Round down to nearest 50mm
            int drawerWidth = (int) (Math.floor(maxPossibleWidth / 50.0) * 50);

            if (drawerWidth < 300) {
                // Fallback to standard plinth if not enough space
                addStandardPlinth(items, cabinRequest, width, depth, thickness, baseboardHeight);
                return;
            }

            // Front panel
            int frontHeight = baseboardHeight - 4;
            items.add(new Item("[Szuflada cokołowa] Front", width - 4, frontHeight, thickness, 1, MaterialType.PLYTA_MEBLOWA, ItemType.PLINTH));

            // Drawer Box
            int boxDepth = Math.min(400, depth - 100);
            int boxHeight = Math.min(60, frontHeight - 20);
            
            // Box front/back (internal)
            int internalFrontWidth = drawerWidth - (2 * thickness);
            items.add(new Item("[Szuflada cokołowa] Plecy", internalFrontWidth, boxHeight, thickness, 2, MaterialType.PLYTA_MEBLOWA, ItemType.DRAWER_BOX));
            // Box bottom
            items.add(new Item("[Szuflada cokołowa] Dno", drawerWidth, boxDepth, thickness, 1, MaterialType.PLYTA_MEBLOWA, ItemType.DRAWER_BOTTOM));
        } else {
            addStandardPlinth(items, cabinRequest, width, depth, thickness, baseboardHeight);
        }
    }

    /**
     * Adds a standard plinth front panel to the items list.
     * Used when plinth drawer is not requested or when there is insufficient space for a drawer.
     * 
     * @param items the list to which the plinth front will be added
     * @param cabinRequest the cabinet configuration
     * @param width the cabinet width in mm
     * @param depth the cabinet depth in mm
     * @param thickness the board thickness in mm
     * @param baseboardHeight the height of the plinth in mm
     */
    private static void addStandardPlinth(List<Item> items, WorkRequest.CabinRequest cabinRequest, int width, int depth, int thickness, int baseboardHeight) {
        // Standard plinth
        // Usually just a front panel, maybe sides if returned?
        // Let's assume just front panel for now.
        items.add(new Item("[Cokół] Front", width, baseboardHeight, thickness, 1, MaterialType.PLYTA_MEBLOWA, ItemType.PLINTH));
    }
}
