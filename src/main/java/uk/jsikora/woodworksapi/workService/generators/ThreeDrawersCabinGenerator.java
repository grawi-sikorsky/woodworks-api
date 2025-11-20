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
public class ThreeDrawersCabinGenerator implements CabinCuttingStrategy {

    private static final int FRONT_CLEARANCE = 4;
    private static final int TOP_BEAM_HEIGHT = 100;
    private static final int BLUM_ANTARO_LW75 = 75;
    private static final int BLUM_ANTARO_LW87 = 87;
    private static final int BLUM_ANTARO_PLECKI_WYS = 116;
    private static final int BLUM_ANTARO_NL = 500;
    private static final int BLUM_ANTARO_THICK = 16;

    @Override
    public boolean supports(CabinType type) {
        return type == CabinType.THREE_DRAWERS;
    }

    @Override
    public List<Item> generateItems(WorkRequest.CabinRequest cabinRequest) {
        log.info("Generating three drawers cabin items.");
        int thickness = cabinRequest.thickness();
        int width = cabinRequest.width();
        int height = cabinRequest.height();
        int depth = cabinRequest.depth();
        int count = cabinRequest.cabinCount();

        int innerWidth = width - 2 * thickness;

        List<Item> items = new ArrayList<>();

        // Boki
        items.add(new Item("Bok (lewy)", depth, height, thickness, 1, PLYTA_MEBLOWA));
        items.add(new Item("Bok (prawy)", depth, height, thickness, 1, PLYTA_MEBLOWA));

        // Spód
        items.add(new Item("Spód", innerWidth, depth, thickness, 1, PLYTA_MEBLOWA));

        // Belka górna
        items.add(new Item("Belka górna", innerWidth, TOP_BEAM_HEIGHT, thickness, 1, PLYTA_MEBLOWA));

        // Plecy
        items.add(new Item("Plecy", width, height, 3, 1, HDF));

        // Fronty Szuflad (3 równe)
        int drawerFrontHeight = (height - FRONT_CLEARANCE) / 3 - 2; // Simplified gap logic
        for (int i = 0; i < 3; i++) {
            items.add(new Item("Front " + (i + 1), width - FRONT_CLEARANCE, drawerFrontHeight, thickness, 1, PLYTA_MEBLOWA));
        }

        // Szuflady (3 komplety)
        for (int i = 0; i < 3; i++) {
            items.add(new Item("Szuflada " + (i + 1) + " plecy", innerWidth - BLUM_ANTARO_LW87, BLUM_ANTARO_PLECKI_WYS, BLUM_ANTARO_THICK, 1, PLYTA_MEBLOWA));
            items.add(new Item("Szuflada " + (i + 1) + " dno", innerWidth - BLUM_ANTARO_LW75, BLUM_ANTARO_NL - 24, BLUM_ANTARO_THICK, 1, PLYTA_MEBLOWA));
        }

        return ItemUtils.aggregateItems(items.stream()
                                             .flatMap(i -> IntStream.range(0, count)
                                                                    .mapToObj(j -> i))
                                             .toList());
    }
}
