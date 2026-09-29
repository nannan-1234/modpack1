package com.reiryoku_expansion.hokora;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.iwaliner.urushi.util.ElementType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Serializer for {@code reiryoku_expansion:hokora_fuel} recipes:
 * {@code {"element": "wood", "ingredient": ..., "reiryoku": 5, "cooldown": 100}}.
 */
public class HokoraFuelRecipeSerializer implements RecipeSerializer<HokoraFuelRecipe> {

    @Override
    public HokoraFuelRecipe m_6729_(ResourceLocation id, JsonObject json) { // fromJson
        if (!json.has("element")) {
            throw new JsonSyntaxException("Missing 'element' in hokora fuel recipe '" + id + "'");
        }
        String elementName = json.get("element").getAsString();
        ElementType element = HokoraFuelConfig.parseElement(elementName);
        if (element == null) {
            throw new JsonSyntaxException(
                    "Unknown reiryoku element '" + elementName + "' in hokora fuel recipe '" + id + "'");
        }
        if (!json.has("ingredient")) {
            throw new JsonSyntaxException("Missing 'ingredient' in hokora fuel recipe '" + id + "'");
        }
        Ingredient ingredient = HokoraFuelConfig.parseIngredient(json.get("ingredient"));
        if (ingredient.m_43908_().length == 0) { // getItems
            throw new JsonSyntaxException(
                    "Hokora fuel ingredient in recipe '" + id + "' resolved to no items");
        }
        int reiryoku = HokoraFuelConfig.parsePositiveInt(json, "reiryoku", 5, id);
        int cooldown = HokoraFuelConfig.parsePositiveInt(json, "cooldown", 100, id);
        return new HokoraFuelRecipe(id, element, ingredient, reiryoku, cooldown);
    }

    @Override
    public HokoraFuelRecipe m_8005_(ResourceLocation id, FriendlyByteBuf buffer) { // fromNetwork
        ElementType element = ElementType.getType(buffer.m_130242_()); // readVarInt
        Ingredient ingredient = Ingredient.m_43940_(buffer); // fromNetwork
        int reiryoku = buffer.m_130242_();
        int cooldown = buffer.m_130242_();
        return new HokoraFuelRecipe(id, element, ingredient, reiryoku, cooldown);
    }

    @Override
    public void m_6178_(FriendlyByteBuf buffer, HokoraFuelRecipe recipe) { // toNetwork
        buffer.m_130130_(recipe.getElement().getID()); // writeVarInt
        recipe.getFuelIngredient().m_43923_(buffer); // toNetwork
        buffer.m_130130_(recipe.getReiryoku());
        buffer.m_130130_(recipe.getCooldown());
    }
}
