package com.reiryoku_expansion.ae2;

import com.iwaliner.urushi.util.interfaces.ReiryokuStorable;
import com.reiryoku_expansion.ReiryokuElement;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

/**
 * Import bus strategy: each operation pulls 1 Reiryoku (more with acceleration
 * cards) out of the adjacent Urushi block and inserts it into the ME network.
 * The external block is drained first; if the network cannot accept the full
 * amount (e.g. no cell capacity), the remainder is refunded.
 */
public class ReiryokuStorageImportStrategy implements StackImportStrategy {

    private final ServerLevel level;
    private final BlockPos pos;

    public ReiryokuStorageImportStrategy(ServerLevel level, BlockPos pos, Direction fromSide) {
        this.level = level;
        this.pos = pos;
    }

    @Override
    public boolean transfer(StackTransferContext context) {
        if (!context.isKeyTypeEnabled(ReiryokuKeyType.INSTANCE)) {
            return false;
        }
        ReiryokuStorable storable = ReiryokuStorables.find(level, pos);
        if (storable == null) {
            return false;
        }
        ReiryokuElement element = ReiryokuStorables.storedElement(storable);
        if (element == null) {
            return false;
        }
        int operations = context.getOperationsRemaining();
        if (operations <= 0) {
            return false;
        }
        int maxAmount = operations * ReiryokuKeyType.INSTANCE.getAmountPerOperation();
        int amount = Math.min(maxAmount, storable.getStoredReiryoku());
        if (amount <= 0) {
            return false;
        }

        // Drain the external block first, then insert into the network and refund
        // anything the network could not take.
        storable.decreaseStoredReiryoku(amount);
        storable.markUpdated();

        long inserted = context.getInternalStorage().getInventory().insert(
                ReiryokuKey.of(element), amount, Actionable.MODULATE, context.getActionSource());
        if (inserted < amount) {
            storable.addStoredReiryoku((int) (amount - inserted));
            storable.markUpdated();
        }
        context.reduceOperationsRemaining(Math.max(1, inserted));
        return inserted > 0;
    }
}
