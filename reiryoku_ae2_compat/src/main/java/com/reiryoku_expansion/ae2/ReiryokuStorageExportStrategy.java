package com.reiryoku_expansion.ae2;

import com.iwaliner.urushi.util.interfaces.ReiryokuStorable;
import com.reiryoku_expansion.ReiryokuElement;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

/**
 * Export bus strategy: pushes Reiryoku out of the ME network into the adjacent
 * Urushi block. Only the element matching the block's stored element type is
 * accepted, and a full block never receives more.
 */
public class ReiryokuStorageExportStrategy implements StackExportStrategy {

    private final ServerLevel level;
    private final BlockPos pos;

    public ReiryokuStorageExportStrategy(ServerLevel level, BlockPos pos, Direction fromSide) {
        this.level = level;
        this.pos = pos;
    }

    @Override
    public long transfer(StackTransferContext context, AEKey what, long amount) {
        if (!(what instanceof ReiryokuKey key)) {
            return 0;
        }
        ReiryokuStorable storable = ReiryokuStorables.find(level, pos);
        if (storable == null) {
            return 0;
        }
        ReiryokuElement element = ReiryokuStorables.storedElement(storable);
        if (element != key.getElement()) {
            return 0;
        }
        long toTransfer = Math.min(amount, freeSpace(storable));
        if (toTransfer <= 0) {
            return 0;
        }
        IEnergySource energy = context.getEnergySource();
        long extracted = StorageHelper.poweredExtraction(
                energy,
                context.getInternalStorage().getInventory(),
                key,
                toTransfer,
                context.getActionSource(),
                Actionable.MODULATE);
        if (extracted > 0) {
            storable.addStoredReiryoku((int) extracted);
            storable.markUpdated();
        }
        return extracted;
    }

    @Override
    public long push(AEKey what, long amount, Actionable mode) {
        if (!(what instanceof ReiryokuKey key)) {
            return 0;
        }
        ReiryokuStorable storable = ReiryokuStorables.find(level, pos);
        if (storable == null) {
            return 0;
        }
        ReiryokuElement element = ReiryokuStorables.storedElement(storable);
        if (element != key.getElement()) {
            return 0;
        }
        long toTransfer = Math.min(amount, freeSpace(storable));
        if (toTransfer <= 0) {
            return 0;
        }
        if (mode == Actionable.MODULATE) {
            storable.addStoredReiryoku((int) toTransfer);
            storable.markUpdated();
        }
        return toTransfer;
    }

    private static int freeSpace(ReiryokuStorable storable) {
        return storable.getReiryokuCapacity() - storable.getStoredReiryoku();
    }
}
