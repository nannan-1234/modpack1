package com.reiryoku_expansion.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;

/**
 * KubeJS addon (discovered via {@code kubejs.plugins.txt}):
 * <ul>
 *   <li>{@code event.recipes.reiryoku_expansion.hokora_fuel(element, ingredient, reiryoku?, cooldown?)}
 *       - Hokora (神龛) fuel conversion recipes.</li>
 *   <li>{@code event.recipes.urushi.<element>_element_tier<1|2>_crafting(ingredients, result, reiryoku)}
 *       - Urushi element crafting table (仪式台) recipes for all five
 *       elements and both tiers.</li>
 * </ul>
 */
public class ReiryokuKubeJSPlugin extends KubeJSPlugin {

    private static final RecipeSchema HOKORA_FUEL = new RecipeSchema(
            new RecipeKey<>(StringComponent.ID, "element"),
            new RecipeKey<>(ItemComponents.INPUT, "ingredient"),
            new RecipeKey<>(NumberComponent.INT, "reiryoku").optional(5),
            new RecipeKey<>(NumberComponent.INT, "cooldown").optional(100)
    );

    private static final RecipeSchema ELEMENT_CRAFTING = new RecipeSchema(
            new RecipeKey<>(ItemComponents.INPUT_ARRAY, "ingredients"),
            new RecipeKey<>(ItemComponents.OUTPUT, "result"),
            new RecipeKey<>(NumberComponent.INT, "reiryoku")
    );

    private static final String[] ELEMENT_CRAFTING_TYPES = {
            "wood_element_tier1_crafting", "wood_element_tier2_crafting",
            "fire_element_tier1_crafting", "fire_element_tier2_crafting",
            "earth_element_tier1_crafting", "earth_element_tier2_crafting",
            "metal_element_tier1_crafting", "metal_element_tier2_crafting",
            "water_element_tier1_crafting", "water_element_tier2_crafting"
    };

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.namespace("reiryoku_expansion").register("hokora_fuel", HOKORA_FUEL);
        for (String type : ELEMENT_CRAFTING_TYPES) {
            event.namespace("urushi").register(type, ELEMENT_CRAFTING);
        }
    }
}
