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
public class NoDoorsCabinGenerator implements CabinCuttingStrategy {

    private static final int TOP_BEAM_HEIGHT = 100;

    @Override
    public boolean supports(CabinType cabinType) {
        return cabinType == CabinType.NO_DOORS;
    }

    @Override
    public List<Item> generateItems(WorkRequest.CabinRequest cabinRequest) {
        log.info("Generating no doors cabin items.");
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

        return ItemUtils.aggregateItems(items.stream()
                                             .flatMap(i -> IntStream.range(0, count)
                                                                    .mapToObj(j -> i))
                                             .toList());
    }
}