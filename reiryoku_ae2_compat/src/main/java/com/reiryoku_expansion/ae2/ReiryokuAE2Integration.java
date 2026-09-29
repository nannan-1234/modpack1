package com.reiryoku_expansion.ae2;

import com.reiryoku_expansion.ReiryokuExpansion;
import com.reiryoku_expansion.client.ReiryokuAE2Client;
import com.reiryoku_expansion.item.ReiryokuCellHandler;
import com.reiryoku_expansion.item.ReiryokuItems;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackImportStrategy;
import appeng.api.client.StorageCellModels;
import appeng.api.ids.AECreativeTabIds;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.storage.StorageCells;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegisterEvent;

/**
 * Everything that touches AE2. Only loaded (and only registered) when AE2 is
 * present, so the mod works without AE2 as well.
 *
 * <p>Registration order follows Applied Botanics 1.5.0, the reference
 * implementation for this AE2 version.</p>
 */
public final class ReiryokuAE2Integration {

    private ReiryokuAE2Integration() {
    }

    public static void init(IEventBus modBus) {
        ReiryokuItems.initialize(modBus);
        modBus.addListener(ReiryokuAE2Integration::onRegister);
        modBus.addListener(ReiryokuAE2Integration::addToCreativeTab);

        // World behaviors are read by both AE2 and ExtendedAE buses. These are
        // the official API entry points (they delegate to AE2's internal
        // StackWorldBehaviors registry, so behavior is identical) - keeping the
        // internal class out of this mod's direct dependencies.
        StackImportStrategy.register(ReiryokuKeyType.INSTANCE, ReiryokuStorageImportStrategy::new);
        StackExportStrategy.register(ReiryokuKeyType.INSTANCE, ReiryokuStorageExportStrategy::new);
        ExternalStorageStrategy.register(ReiryokuKeyType.INSTANCE, ReiryokuExternalStorageStrategy::new);

        // Pattern encoding / terminal generic-stack support.
        ContainerItemStrategy.register(ReiryokuKeyType.INSTANCE, ReiryokuKey.class, new ReiryokuContainerItemStrategy());
        GenericSlotCapacities.register(ReiryokuKeyType.INSTANCE, 256_000L);

        StorageCells.addCellHandler(ReiryokuCellHandler.INSTANCE);

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> (DistExecutor.SafeRunnable) () -> ReiryokuAE2Client.init());

        modBus.addListener(ReiryokuAE2Integration::commonSetup);
    }

    /**
     * Adds the cells to AE2's main creative tab so they are easy to find next
     * to the vanilla storage cells.
     */
    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(AECreativeTabIds.MAIN)) {
            return;
        }
        event.m_246326_(ReiryokuItems.REIRYOKU_CELL_HOUSING.get()); // accept(ItemLike)
        event.m_246326_(ReiryokuItems.REIRYOKU_STORAGE_CELL_1K.get());
        event.m_246326_(ReiryokuItems.REIRYOKU_STORAGE_CELL_4K.get());
        event.m_246326_(ReiryokuItems.REIRYOKU_STORAGE_CELL_16K.get());
        event.m_246326_(ReiryokuItems.REIRYOKU_STORAGE_CELL_64K.get());
        event.m_246326_(ReiryokuItems.REIRYOKU_STORAGE_CELL_256K.get());
    }

    private static void onRegister(RegisterEvent event) {
        // AE2 assigns raw key type ids during the registry phase (same as
        // Applied Botanics does for its mana key type).
        if (event.getRegistryKey().equals(Registries.f_256747_)) {
            AEKeyTypes.register(ReiryokuKeyType.INSTANCE);
        }
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Drive cell models must be registered after the items exist.
            StorageCellModels.registerModel(ReiryokuItems.REIRYOKU_STORAGE_CELL_1K.get(),
                    ReiryokuExpansion.id("block/drive/cells/reiryoku_storage_cell_1k"));
            StorageCellModels.registerModel(ReiryokuItems.REIRYOKU_STORAGE_CELL_4K.get(),
                    ReiryokuExpansion.id("block/drive/cells/reiryoku_storage_cell_4k"));
            StorageCellModels.registerModel(ReiryokuItems.REIRYOKU_STORAGE_CELL_16K.get(),
                    ReiryokuExpansion.id("block/drive/cells/reiryoku_storage_cell_16k"));
            StorageCellModels.registerModel(ReiryokuItems.REIRYOKU_STORAGE_CELL_64K.get(),
                    ReiryokuExpansion.id("block/drive/cells/reiryoku_storage_cell_64k"));
            StorageCellModels.registerModel(ReiryokuItems.REIRYOKU_STORAGE_CELL_256K.get(),
                    ReiryokuExpansion.id("block/drive/cells/reiryoku_storage_cell_256k"));
        });
    }
}
