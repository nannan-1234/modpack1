package com.reiryoku_expansion;

import com.iwaliner.urushi.util.ElementType;
import net.minecraft.resources.ResourceLocation;

/**
 * The five Reiryoku element types. We keep our own enum instead of reusing
 * Urushi's {@link ElementType} directly so the AE2 key semantics stay stable
 * (five valid elements, never {@code FAIL}).
 *
 * <p>ElementType IDs (Urushi 6.6.3): 0=Wood, 1=Fire, 2=Earth, 3=Metal,
 * 4=Water, -1=FAIL.</p>
 */
public enum ReiryokuElement {
    WOOD("wood", "wood", ElementType.WoodElement, 0xFF55FF55),
    FIRE("fire", "fire", ElementType.FireElement, 0xFFFF5555),
    EARTH("earth", "earth", ElementType.EarthElement, 0xFFFFAA00),
    METAL("metal", "metal", ElementType.MetalElement, 0xFFE0E0E0),
    WATER("water", "water", ElementType.WaterElement, 0xFF5599FF);

    /** NBT key used inside the cell inventory (must be stable for old saves). */
    private final String nbtKey;
    /** Path used for the AE2 key id / lang key. */
    private final String idPath;
    /** Corresponding Urushi element type. */
    private final ElementType urushiType;
    /** ARGB color used for in-world / GUI accents (mirrors Urushi's element colors). */
    private final int argbColor;
    private final ResourceLocation id;

    ReiryokuElement(String nbtKey, String idPath, ElementType urushiType, int argbColor) {
        this.nbtKey = nbtKey;
        this.idPath = idPath;
        this.urushiType = urushiType;
        this.argbColor = argbColor;
        this.id = new ResourceLocation("reiryoku_expansion", idPath);
    }

    public String getNbtKey() {
        return nbtKey;
    }

    /**
     * Stable data-format ID used for AE2 key NBT and network serialization.
     * These values are part of the persisted data format (patterns, generic
     * stacks, network packets) and must never change or be derived from the
     * enum declaration order. Adding a new element must not alter existing IDs.
     */
    public String getDataId() {
        return nbtKey;
    }

    public String getLangKey() {
        return idPath;
    }

    public ElementType toUrushi() {
        return urushiType;
    }

    public ResourceLocation getId() {
        return id;
    }

    public int getArgbColor() {
        return argbColor;
    }

    /** Maps a Urushi element type to ours; returns {@code null} for FAIL/null. */
    public static ReiryokuElement fromUrushi(ElementType type) {
        if (type == null || type == ElementType.FAIL) {
            return null;
        }
        for (ReiryokuElement element : values()) {
            if (element.urushiType == type) {
                return element;
            }
        }
        return null;
    }

    /** Looks up an element by its stable data id; returns {@code null} for unknown ids. */
    public static ReiryokuElement byDataId(String dataId) {
        for (ReiryokuElement element : values()) {
            if (element.nbtKey.equals(dataId)) {
                return element;
            }
        }
        return null;
    }

    /**
     * LEGACY ONLY: decodes the pre-1.2.1 format where the AE2 key NBT stored
     * the enum ordinal as an int. Do not use this for new data.
     */
    public static ReiryokuElement byLegacyOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) {
            return null;
        }
        return values()[ordinal];
    }
}
