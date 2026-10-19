package com.reiryoku_expansion.hokora;

import com.iwaliner.urushi.util.ElementType;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Data-carrier recipe for Hokora (神龛) fuel. It never matches anything in a
 * crafting grid; it exists so the conversion can be written as a regular
 * {@code data/<namespace>/recipes/*.json} recipe (and therefore through
 * KubeJS) instead of a hardcoded map.
 */
public class HokoraFuelRecipe implements Recipe<Container> {

    public static final RecipeType<HokoraFuelRecipe> TYPE = new RecipeType<>() {
    };

    private final ResourceLocation id;
    private final ElementType element;
    private final Ingredient ingredient;
    private final int reiryoku;
    private final int cooldown;

    public HokoraFuelRecipe(ResourceLocation id, ElementType element, Ingredient ingredient, int reiryoku, int cooldown) {
        this.id = id;
        this.element = element;
        this.ingredient = ingredient;
        this.reiryoku = reiryoku;
        this.cooldown = cooldown;
    }

    public ElementType getElement() {
        return element;
    }

    public Ingredient getFuelIngredient() {
        return ingredient;
    }

    public int getReiryoku() {
        return reiryoku;
    }

    public int getCooldown() {
        return cooldown;
    }

    @Override
    public boolean matches(Container inv, Level level) { // matches
        return false;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess access) { // assemble
        return ItemStack.EMPTY; // EMPTY
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { // canCraftInDimensions
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) { // getResultItem
        return ItemStack.EMPTY; // EMPTY
    }

    @Override
    public ResourceLocation getId() { // getId
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() { // getSerializer
        return HokoraFuelRecipes.HOKORA_FUEL_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() { // getType
        return TYPE;
    }
}
