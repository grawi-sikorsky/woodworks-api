package uk.jsikora.woodworksapi.workService;

import java.util.List;
import java.util.stream.Collectors;

public class ItemUtils {

    public static List<Item> aggregateItems(List<Item> items) {
        return items.stream()
                    .collect(Collectors.groupingBy(item -> new ItemKey(item.name(), item.width(), item.height(), item.thickness(), item.material()),
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
                                                .material()))
                    .toList();
    }

    private record ItemKey(String name, int width, int height, int thickness, MaterialType material) {}
}
