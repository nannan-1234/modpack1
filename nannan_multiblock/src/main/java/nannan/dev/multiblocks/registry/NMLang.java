package nannan.dev.multiblocks.registry;

import mekanism.api.text.ILangEntry;
import nannan.dev.multiblocks.NannanMultiblocks;

/**
 * 本模组的语言键。
 *
 * <p>{@code DESCRIPTION_*} 用于方块类型（{@code BlockTypeTile}）的说明文本，键名沿用 Mekanism 的
 * {@code description.<modid>.<path>} 习惯；其余键用于机器名等。</p>
 */
public enum NMLang implements ILangEntry {

    DESCRIPTION_CONTROL_FURNACE_CASING("description." + NannanMultiblocks.MODID + ".control_furnace_casing"),
    DESCRIPTION_CONTROL_FURNACE_CONTROLLER("description." + NannanMultiblocks.MODID + ".control_furnace_controller"),
    DESCRIPTION_CONTROL_FURNACE_VALVE("description." + NannanMultiblocks.MODID + ".control_furnace_valve"),
    DESCRIPTION_CONTROL_FURNACE_CORE("description." + NannanMultiblocks.MODID + ".control_furnace_core"),
    NEEDS_CORE("multiblock." + NannanMultiblocks.MODID + ".needs_core"),
    VALVE_MODE_INPUT_MATERIAL("valve_mode." + NannanMultiblocks.MODID + ".input_material"),
    VALVE_MODE_INPUT_FUEL("valve_mode." + NannanMultiblocks.MODID + ".input_fuel"),
    VALVE_MODE_INPUT_COOLANT("valve_mode." + NannanMultiblocks.MODID + ".input_coolant"),
    VALVE_MODE_OUTPUT_PRODUCT("valve_mode." + NannanMultiblocks.MODID + ".output_product"),
    VALVE_MODE_OUTPUT_COOLANT("valve_mode." + NannanMultiblocks.MODID + ".output_coolant"),
    CONTROL_FURNACE(NannanMultiblocks.MODID + ".control_furnace"),

    // 方块实体的 getDisplayName() 用 "container.<modid>.<方块注册名>" 这个键（Mekanism 约定），
    // 少了它界面上会直接显示原始键名（如 container.nannan_multiblock.control_furnace_controller）。
    CONTAINER_CONTROL_FURNACE_CASING("container." + NannanMultiblocks.MODID + ".control_furnace_casing"),
    CONTAINER_CONTROL_FURNACE_CONTROLLER("container." + NannanMultiblocks.MODID + ".control_furnace_controller"),
    CONTAINER_CONTROL_FURNACE_VALVE("container." + NannanMultiblocks.MODID + ".control_furnace_valve"),
    CONTAINER_CONTROL_FURNACE_CORE("container." + NannanMultiblocks.MODID + ".control_furnace_core"),

    // ---- GUI（里程碑⑥；键名沿用 gui.<modid>.<path> 习惯，文案见 assets/<modid>/lang/） ----
    GUI_STATS_TITLE("gui." + NannanMultiblocks.MODID + ".stats_title"),
    GUI_TAB_MAIN("gui." + NannanMultiblocks.MODID + ".tab.main"),
    GUI_TAB_STATS("gui." + NannanMultiblocks.MODID + ".tab.stats"),
    GUI_STATUS("gui." + NannanMultiblocks.MODID + ".status"),
    GUI_STATUS_RUNNING("gui." + NannanMultiblocks.MODID + ".status.running"),
    GUI_STATUS_IDLE("gui." + NannanMultiblocks.MODID + ".status.idle"),
    GUI_STATUS_NO_FUEL("gui." + NannanMultiblocks.MODID + ".status.no_fuel"),
    GUI_STATUS_SCRAMMED("gui." + NannanMultiblocks.MODID + ".status.scrammed"),
    GUI_BURN_RATE("gui." + NannanMultiblocks.MODID + ".burn_rate"),
    GUI_BURN_RATE_VALUE("gui." + NannanMultiblocks.MODID + ".burn_rate.value"),
    GUI_FLUX("gui." + NannanMultiblocks.MODID + ".flux"),
    GUI_FLUX_VALUE("gui." + NannanMultiblocks.MODID + ".flux.value"),
    GUI_FLUX_ITEM("gui." + NannanMultiblocks.MODID + ".flux.item"),
    GUI_TEMPERATURE("gui." + NannanMultiblocks.MODID + ".temperature"),
    GUI_TEMP_VALUE("gui." + NannanMultiblocks.MODID + ".temperature.value"),
    GUI_TEMPERATURE_REFERENCE("gui." + NannanMultiblocks.MODID + ".temperature.reference"),
    GUI_STABLE("gui." + NannanMultiblocks.MODID + ".stable"),
    GUI_STABLE_VALUE("gui." + NannanMultiblocks.MODID + ".stable.value"),
    GUI_RATE_SLIDER("gui." + NannanMultiblocks.MODID + ".rate_slider"),
    GUI_RATE_PERCENT("gui." + NannanMultiblocks.MODID + ".rate_percent"),
    GUI_FUEL("gui." + NannanMultiblocks.MODID + ".fuel"),
    GUI_COOLANT("gui." + NannanMultiblocks.MODID + ".coolant"),
    GUI_COOLANT_WATER("gui." + NannanMultiblocks.MODID + ".coolant.water"),
    GUI_COOLANT_SODIUM("gui." + NannanMultiblocks.MODID + ".coolant.sodium"),
    GUI_COOLANT_NONE("gui." + NannanMultiblocks.MODID + ".coolant.none"),
    GUI_PRODUCT("gui." + NannanMultiblocks.MODID + ".product"),
    GUI_PRODUCT_STEAM("gui." + NannanMultiblocks.MODID + ".product.steam"),
    GUI_PRODUCT_SODIUM("gui." + NannanMultiblocks.MODID + ".product.sodium"),
    GUI_PRODUCT_NONE("gui." + NannanMultiblocks.MODID + ".product.none"),
    GUI_CURVE("gui." + NannanMultiblocks.MODID + ".curve"),
    GUI_RECIPE_PENDING("gui." + NannanMultiblocks.MODID + ".recipe_pending"),
    GUI_RECIPE_LINE("gui." + NannanMultiblocks.MODID + ".recipe_line"),
    GUI_RECIPE_NONE("gui." + NannanMultiblocks.MODID + ".recipe_none"),
    GUI_RECIPE_PROGRESS("gui." + NannanMultiblocks.MODID + ".recipe_progress"),
    // JEI 类别
    GUI_JEI_CATEGORY("gui." + NannanMultiblocks.MODID + ".jei.category"),
    GUI_JEI_TEMPERATURE("gui." + NannanMultiblocks.MODID + ".jei.temperature"),
    GUI_JEI_FLUX("gui." + NannanMultiblocks.MODID + ".jei.flux"),
    GUI_JEI_STABLE("gui." + NannanMultiblocks.MODID + ".jei.stable"),
    GUI_INVENTORY("gui." + NannanMultiblocks.MODID + ".inventory"),
    GUI_SECTION_HEAT("gui." + NannanMultiblocks.MODID + ".section.heat"),
    GUI_SECTION_FUEL("gui." + NannanMultiblocks.MODID + ".section.fuel"),
    GUI_SECTION_REACTION("gui." + NannanMultiblocks.MODID + ".section.reaction"),
    GUI_SHELLS_CORES("gui." + NannanMultiblocks.MODID + ".shells_cores"),
    GUI_HEAT_CAPACITY("gui." + NannanMultiblocks.MODID + ".heat_capacity"),
    GUI_MAX_HEAT("gui." + NannanMultiblocks.MODID + ".max_heat"),
    GUI_CONDUCTIVITY("gui." + NannanMultiblocks.MODID + ".conductivity"),
    GUI_MAX_BURN("gui." + NannanMultiblocks.MODID + ".max_burn"),
    GUI_RATE_LIMIT("gui." + NannanMultiblocks.MODID + ".rate_limit"),
    GUI_HEAT_PER_MB("gui." + NannanMultiblocks.MODID + ".heat_per_mb"),
    GUI_CURRENT_BURN("gui." + NannanMultiblocks.MODID + ".current_burn");

    private final String translationKey;

    NMLang(String translationKey) {
        this.translationKey = translationKey;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }
}
