package uk.jsikora.woodworksapi.workService;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static uk.jsikora.woodworksapi.workService.MaterialType.HDF;
import static uk.jsikora.woodworksapi.workService.MaterialType.PLYTA_MEBLOWA;

@Component
public class TwoDoorsCabinGenerator implements CabinCuttingStrategy {

    private static final int FRONT_CLEARANCE = 4;
    private static final int TOP_BEAM_HEIGHT = 100;

    @Override
    public boolean supports(CabinType cabinType) {
        return cabinType == CabinType.TWO_DOORS;
    }

    @Override
    public List<Item> generateItems(WorkRequest.CabinRequest cabinRequest) {
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

        // Front
        items.add(new Item("Front Lewy", width / 2 - FRONT_CLEARANCE, height - FRONT_CLEARANCE, thickness, 1, PLYTA_MEBLOWA));
        items.add(new Item("Front Prawy", width / 2 - FRONT_CLEARANCE, height - FRONT_CLEARANCE, thickness, 1, PLYTA_MEBLOWA));

//        // Uwzględnij ilość szafek
//        return items.stream()
//                    .map(i -> new Item(i.name(), i.width(), i.height(), i.thickness(), 1, i.material()))
//                    .flatMap(i -> IntStream.range(0, count).mapToObj(j -> i))
//                    .toList();

        return ItemUtils.aggregateItems(items.stream()
                                             .flatMap(i -> IntStream.range(0, count)
                                                                    .mapToObj(j -> i))
                                             .toList());
    }
}