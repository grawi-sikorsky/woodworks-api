package uk.jsikora.woodworksapi.workService;

/**
 *
 * @param name
 * @param width
 * @param height
 * @param thickness
 * @param material
 */
public record Item(String name, int width, int height, int thickness, int count, MaterialType material) {}
