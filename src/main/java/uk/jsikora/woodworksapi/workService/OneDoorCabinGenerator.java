package uk.jsikora.woodworksapi.workService;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static uk.jsikora.woodworksapi.workService.MaterialType.HDF;
import static uk.jsikora.woodworksapi.workService.MaterialType.PLYTA_MEBLOWA;

@Component
public class OneDoorCabinGenerator implements CabinCuttingStrategy {

    private static final int FRONT_CLEARANCE = 4;
    private static final int TOP_BEAM_HEIGHT = 100;

    @Override
    public boolean supports(CabinType cabinType) {
        return cabinType == CabinType.ONE_DOOR;
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
        items.add(new Item("Bok (lewy)", depth, height, thickness, PLYTA_MEBLOWA));
        items.add(new Item("Bok (prawy)", depth, height, thickness, PLYTA_MEBLOWA));

        // Spód
        items.add(new Item("Spód", innerWidth, depth, thickness, PLYTA_MEBLOWA));

        // Belka górna
        items.add(new Item("Belka górna", innerWidth, TOP_BEAM_HEIGHT, thickness, PLYTA_MEBLOWA));

        // Plecy
        items.add(new Item("Plecy", width, height, 3, HDF));

        // Front
        items.add(new Item("Front", width - FRONT_CLEARANCE, height - FRONT_CLEARANCE, thickness, PLYTA_MEBLOWA));

        // Uwzględnij ilość szafek
        return items.stream()
                    .map(i -> new Item(i.name(), i.width(), i.height(), i.thickness(), i.material()))
                    .flatMap(i -> IntStream.range(0, count).mapToObj(j -> i))
                    .toList();
    }
}