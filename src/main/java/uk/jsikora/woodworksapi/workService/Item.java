package uk.jsikora.woodworksapi.workService;

import uk.jsikora.woodworksapi.workService.generators.ItemType;

/**
 *
 * @param name
 * @param width
 * @param height
 * @param thickness
 * @param material
 * @param type
 */
public record Item(String name, int width, int height, int thickness, int count, MaterialType material, ItemType type) {}
