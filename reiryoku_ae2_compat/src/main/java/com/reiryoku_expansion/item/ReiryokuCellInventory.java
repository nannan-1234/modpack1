package com.reiryoku_expansion.item;

import com.reiryoku_expansion.ReiryokuElement;
import com.reiryoku_expansion.ae2.ReiryokuKey;
import com.reiryoku_expansion.ae2.ReiryokuKeyType;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Cell inventory: stores up to five element amounts in the item NBT under
 * {@code "reiryoku": {"wood": n, "fire": n, ...}}. The five elements share one
 * byte budget (1 Reiryoku = 1 byte), so total storage is
 * {@code totalBytes * amountPerByte}.
 */
public class ReiryokuCellInventory implements StorageCell {

    private static final String REIRYOKU_TAG = "reiryoku";

    private final IReiryokuCellItem cellType;
    private final ItemStack i;
    private final ISaveProvider container;
    private final long[] stored = new long[ReiryokuElement.values().length];
    private boolean isPersisted = true;

    public ReiryokuCellInventory(IReiryokuCellItem cellType, ItemStack is, ISaveProvider container) {
        this.cellType = cellType;
        this.i = is;
        this.container = container;
        CompoundTag root = i.m_41784_(); // getOrCreateTag
        CompoundTag sub = root.m_128469_(REIRYOKU_TAG); // getCompound
        for (ReiryokuElement element : ReiryokuElement.values()) {
            stored[element.ordinal()] = sub.m_128454_(element.getNbtKey()); // getLong
        }
    }

    private CompoundTag getTag() {
        return i.m_41784_(); // getOrCreateTag
    }

    public long getStored(ReiryokuElement element) {
        return stored[element.ordinal()];
    }

    private long getTotalStored() {
        long total = 0;
        for (long amount : stored) {
            total += amount;
        }
        return total;
    }

    private long getMaxReiryoku() {
        return cellType.getTotalBytes() * ReiryokuKeyType.INSTANCE.getAmountPerByte();
    }

    public long getTotalBytes() {
        return cellType.getTotalBytes();
    }

    public long getUsedBytes() {
        long amountPerByte = ReiryokuKeyType.INSTANCE.getAmountPerByte();
        return (getTotalStored() + amountPerByte - 1) / amountPerByte;
    }

    @Override
    public CellState getStatus() {
        long stored = getTotalStored();
        long max = getMaxReiryoku();
        if (stored == 0) {
            return CellState.EMPTY;
        }
        if (stored == max) {
            return CellState.FULL;
        }
        if (stored > max / 2) {
            return CellState.TYPES_FULL;
        }
        return CellState.NOT_EMPTY;
    }

    @Override
    public double getIdleDrain() {
        return cellType.getIdleDrain();
    }

    protected void saveChanges() {
        isPersisted = false;
        if (container != null) {
            container.saveChanges();
        } else {
            persist();
        }
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof ReiryokuKey key)) {
            return 0;
        }
        long free = getMaxReiryoku() - getTotalStored();
        long toAdd = Math.min(free, amount);
        if (toAdd <= 0) {
            return 0;
        }
        if (mode == Actionable.MODULATE) {
            stored[key.getElement().ordinal()] += toAdd;
            saveChanges();
        }
        return toAdd;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof ReiryokuKey key)) {
            return 0;
        }
        int index = key.getElement().ordinal();
        long toExtract = Math.min(stored[index], amount);
        if (toExtract <= 0) {
            return 0;
        }
        if (mode == Actionable.MODULATE) {
            stored[index] -= toExtract;
            saveChanges();
        }
        return toExtract;
    }

    @Override
    public void persist() {
        if (isPersisted) {
            return;
        }
        CompoundTag root = getTag();
        CompoundTag sub = new CompoundTag();
        for (ReiryokuElement element : ReiryokuElement.values()) {
            long amount = stored[element.ordinal()];
            if (amount > 0) {
                sub.m_128356_(element.getNbtKey(), amount); // putLong
            }
        }
        if (sub.m_128456_()) { // isEmpty
            root.m_128473_(REIRYOKU_TAG); // remove
        } else {
            root.m_128365_(REIRYOKU_TAG, sub); // put
        }
        isPersisted = true;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (ReiryokuElement element : ReiryokuElement.values()) {
            long amount = stored[element.ordinal()];
            if (amount > 0) {
                out.add(ReiryokuKey.of(element), amount);
            }
        }
    }

    @Override
    public Component getDescription() {
        return i.m_41786_(); // getHoverName
    }
}
