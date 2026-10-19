package com.reiryoku_expansion.item;

import java.util.List;

import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import appeng.core.localization.Tooltips;
import com.reiryoku_expansion.ReiryokuElement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Cell handler that makes {@link IReiryokuCellItem} items usable in ME drives
 * and chests.
 */
public class ReiryokuCellHandler implements ICellHandler {

    public static final ReiryokuCellHandler INSTANCE = new ReiryokuCellHandler();

    @Override
    public boolean isCell(ItemStack is) {
        return !is.isEmpty() && is.getItem() instanceof IReiryokuCellItem; // isEmpty, getItem
    }

    @Override
    public StorageCell getCellInventory(ItemStack is, ISaveProvider container) {
        if (!isCell(is)) {
            return null;
        }
        return new ReiryokuCellInventory((IReiryokuCellItem) is.getItem(), is, container);
    }

    public void addCellInformationToTooltip(ItemStack is, List<Component> lines) {
        ReiryokuCellInventory cell = (ReiryokuCellInventory) getCellInventory(is, null);
        if (cell == null) {
            return;
        }
        lines.add(Tooltips.bytesUsed(cell.getUsedBytes(), cell.getTotalBytes()));
        for (ReiryokuElement element : ReiryokuElement.values()) {
            long stored = cell.getStored(element);
            if (stored > 0) {
                lines.add(Component.translatable("key.reiryoku_expansion." + element.getLangKey())
                        .append(": " + stored) // append
                        .withStyle(ChatFormatting.GREEN)); // withStyle
            }
        }
    }
}
