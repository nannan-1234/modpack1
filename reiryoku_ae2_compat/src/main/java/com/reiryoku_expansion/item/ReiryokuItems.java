package com.reiryoku_expansion.item;

import com.reiryoku_expansion.ReiryokuExpansion;

import appeng.core.definitions.AEItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Item registration for the Reiryoku cells and their housing.
 */
public final class ReiryokuItems {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ReiryokuExpansion.MODID);

    public static final RegistryObject<Item> REIRYOKU_CELL_HOUSING = ITEMS.register("reiryoku_cell_housing",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> REIRYOKU_STORAGE_CELL_1K = cell("reiryoku_storage_cell_1k",
            AEItems.CELL_COMPONENT_1K, 1, 0.5);
    public static final RegistryObject<Item> REIRYOKU_STORAGE_CELL_4K = cell("reiryoku_storage_cell_4k",
            AEItems.CELL_COMPONENT_4K, 4, 1.0);
    public static final RegistryObject<Item> REIRYOKU_STORAGE_CELL_16K = cell("reiryoku_storage_cell_16k",
            AEItems.CELL_COMPONENT_16K, 16, 1.5);
    public static final RegistryObject<Item> REIRYOKU_STORAGE_CELL_64K = cell("reiryoku_storage_cell_64k",
            AEItems.CELL_COMPONENT_64K, 64, 2.0);
    public static final RegistryObject<Item> REIRYOKU_STORAGE_CELL_256K = cell("reiryoku_storage_cell_256k",
            AEItems.CELL_COMPONENT_256K, 256, 2.5);

    private ReiryokuItems() {
    }

    private static RegistryObject<Item> cell(String name, ItemLike coreItem, int kBytes, double idleDrain) {
        return ITEMS.register(name, () -> new ReiryokuCellItem(new Item.Properties(), coreItem, kBytes, idleDrain));
    }

    public static void initialize(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
