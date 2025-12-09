package uk.jsikora.woodworksapi.workService;

import uk.jsikora.woodworksapi.workService.generators.ItemType;

public record Item(String name, int width, int height, int thickness, int count, MaterialType material, ItemType type) {}
