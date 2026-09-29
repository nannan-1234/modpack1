package nannan.dev.multiblocks.phasechange;

import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import nannan.dev.multiblocks.registry.NMLang;
import net.minecraft.util.StringRepresentable;

/**
 * 阀门模式（对应裂变反应堆端口的"输入/输出冷却剂/输出废料"，这里按本机需要扩展成 5 种）。
 *
 * <p>模式决定这个阀门对外暴露哪种能力：物品/燃料/冷却剂输入，或产物/冷却剂输出。
 * 颜色用于读数与将来的 GUI 标记（与用户在 2026-09-28 指定的配色一致）。</p>
 */
public enum ValveMode implements StringRepresentable {

    INPUT_MATERIAL("input_material", NMLang.VALVE_MODE_INPUT_MATERIAL, EnumColor.BRIGHT_GREEN),
    INPUT_FUEL("input_fuel", NMLang.VALVE_MODE_INPUT_FUEL, EnumColor.BROWN),
    INPUT_COOLANT("input_coolant", NMLang.VALVE_MODE_INPUT_COOLANT, EnumColor.AQUA),
    OUTPUT_PRODUCT("output_product", NMLang.VALVE_MODE_OUTPUT_PRODUCT, EnumColor.YELLOW),
    OUTPUT_COOLANT("output_coolant", NMLang.VALVE_MODE_OUTPUT_COOLANT, EnumColor.WHITE);

    private static final ValveMode[] MODES = values();

    private final String name;
    private final ILangEntry langEntry;
    private final EnumColor color;

    ValveMode(String name, ILangEntry langEntry, EnumColor color) {
        this.name = name;
        this.langEntry = langEntry;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public ILangEntry getLangEntry() {
        return langEntry;
    }

    public EnumColor getColor() {
        return color;
    }

    /** 用配置器（配置模式）右键阀门时循环：材料 → 燃料 → 冷却剂 → 产物 → 冷却剂输出 → 回到材料。 */
    public ValveMode next() {
        return MODES[(ordinal() + 1) % MODES.length];
    }

    /**
     * 方块状态属性用的名字（`StringRepresentable.getSerializedName()` 的 SRG 名是 m_7912_）。
     * 与 {@link #getName()} 相同，也就是 blockstate JSON 里 `mode=input_material` 的那一串。
     */
    @Override
    public String m_7912_() {
        return name;
    }

    /** 旧存档兼容：认不出的名字都退回默认模式。 */
    public static ValveMode byName(String name) {
        for (ValveMode mode : MODES) {
            if (mode.name.equals(name)) {
                return mode;
            }
        }
        return INPUT_MATERIAL;
    }
}
