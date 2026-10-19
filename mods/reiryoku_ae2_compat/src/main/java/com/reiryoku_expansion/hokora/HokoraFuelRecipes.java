package com.reiryoku_expansion.hokora;

import com.reiryoku_expansion.ReiryokuExpansion;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registers the {@code reiryoku_expansion:hokora_fuel} recipe serializer so vanilla
 * and KubeJS treat Hokora fuel entries as normal recipes.
 */
public final class HokoraFuelRecipes {

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ReiryokuExpansion.MODID);

    public static final RegistryObject<HokoraFuelRecipeSerializer> HOKORA_FUEL_SERIALIZER =
            SERIALIZERS.register("hokora_fuel", HokoraFuelRecipeSerializer::new);

    private HokoraFuelRecipes() {
    }

    public static void initialize(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }
}
