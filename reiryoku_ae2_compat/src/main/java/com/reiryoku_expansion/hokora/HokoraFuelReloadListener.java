package com.reiryoku_expansion.hokora;

import java.util.EnumMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.iwaliner.urushi.util.ElementType;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads {@code data/<namespace>/reiryoku_hokora/*.json} like a recipe:
 *
 * <pre>{@code
 * {
 *   "wood":  { "ingredient": "minecraft:sweet_berries", "reiryoku": 5, "cooldown": 100 },
 *   "fire":  { "ingredient": { "tag": "minecraft:wheat" }, "reiryoku": 5 },
 *   ...
 * }
 * }</pre>
 *
 * <p>Keys are element names (wood/fire/earth/metal/water); {@code ingredient}
 * accepts the same forms as a recipe ingredient (string item id, {@code item}
 * object or {@code tag} object). Missing fields fall back to the mod defaults
 * (5 Reiryoku, 100 ticks). A pack that puts a file at the same path replaces
 * this one entirely; entries only present in the loaded file take effect.</p>
 */
public class HokoraFuelReloadListener extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LoggerFactory.getLogger("reiryoku_expansion");
    private static final String FOLDER = "reiryoku_hokora";
    private static final Gson GSON = new Gson();

    public HokoraFuelReloadListener() {
        super(GSON, FOLDER);
    }

    @Override
    protected void m_5787_(Map<ResourceLocation, JsonElement> jsons, ResourceManager manager, ProfilerFiller profiler) { // apply
        EnumMap<ElementType, HokoraFuelConfig.FuelEntry> fuels = new EnumMap<>(ElementType.class);
        jsons.forEach((id, json) -> {
            try {
                JsonObject root = json.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                    ElementType element = HokoraFuelConfig.parseElement(entry.getKey());
                    if (element == null) {
                        LOGGER.error("Unknown reiryoku element '{}' in hokora fuel config '{}'", entry.getKey(), id);
                        continue;
                    }
                    JsonObject config = entry.getValue().getAsJsonObject();
                    Ingredient ingredient = parseIngredient(config);
                    if (ingredient == null) {
                        LOGGER.error("Missing 'ingredient' for element '{}' in hokora fuel config '{}'",
                                entry.getKey(), id);
                        continue;
                    }
                    ItemStack[] stacks = ingredient.m_43908_(); // getItems
                    if (stacks.length == 0) {
                        LOGGER.error("Hokora fuel ingredient for element '{}' in hokora fuel config '{}' "
                                + "resolved to no items", entry.getKey(), id);
                        continue;
                    }
                    int reiryoku = HokoraFuelConfig.parsePositiveInt(config, "reiryoku", 5, id);
                    int cooldown = HokoraFuelConfig.parsePositiveInt(config, "cooldown", 100, id);
                    fuels.put(element, new HokoraFuelConfig.FuelEntry(
                            ingredient, stacks[0].m_41720_(), reiryoku, cooldown));
                }
            } catch (Exception e) {
                LOGGER.error("Failed to parse hokora fuel config {}", id, e);
            }
        });
        HokoraFuelConfig.reloadLegacy(fuels);
    }

    private static Ingredient parseIngredient(JsonObject config) {
        if (!config.has("ingredient")) {
            return null;
        }
        return HokoraFuelConfig.parseIngredient(config.get("ingredient"));
    }

}
