package com.reiryoku_expansion.item;

import java.util.List;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.StorageCell;
import appeng.util.ConfigInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

/**
 * ME Reiryoku storage cell. Mirrors Applied Botanics' mana cell: the tier's
 * storage component is stored for disassembly, the cell holds
 * {@code kBytes * 1000} Reiryoku (1 byte = 1 Reiryoku).
 */
public class ReiryokuCellItem extends Item implements IReiryokuCellItem {

    private final ItemLike coreItem;
    private final long totalBytes;
    private final double idleDrain;

    public ReiryokuCellItem(Item.Properties properties, ItemLike coreItem, int kBytes, double idleDrain) {
        super(properties.m_41487_(1)); // stacksTo(1)
        this.coreItem = coreItem;
        this.totalBytes = kBytes * 1000L;
        this.idleDrain = idleDrain;
    }

    @Override
    public long getTotalBytes() {
        return totalBytes;
    }

    @Override
    public double getIdleDrain() {
        return idleDrain;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return false;
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return null;
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        return null;
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        // not partitionable
    }

    @Override
    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) { // use()
        ItemStack stack = player.m_21120_(hand); // getItemInHand
        disassembleDrive(stack, level, player);
        return new InteractionResultHolder<>(InteractionResult.m_19078_(level.m_5776_()), stack); // sidedSuccess, isClientSide
    }

    @Override
    public InteractionResult m_6225_(UseOnContext context) { // onItemUseFirst()
        Level level = context.m_43725_(); // getLevel
        Player player = context.m_43723_(); // getPlayer
        if (disassembleDrive(player.m_21120_(context.m_43724_()), level, player)) { // getItemInHand(getHand)
            return InteractionResult.m_19078_(level.m_5776_());
        }
        return InteractionResult.PASS;
    }

    /**
     * Sneak-right-click an empty cell to recover the storage component plus the
     * cell housing, exactly like Applied Botanics' mana cells.
     *
     * <p>{@code Inventory.placeItemBackInInventory} (m_150079_) puts the stack
     * into the inventory and drops any leftover at the player's feet, so the
     * products can never vanish. The products are secured before the cell is
     * consumed, so even a mid-operation failure leaves the player with either
     * the cell in hand or the products in inventory/on the ground.</p>
     */
    private boolean disassembleDrive(ItemStack stack, Level level, Player player) {
        if (!player.m_6144_() || level.m_5776_()) { // isShiftKeyDown, isClientSide
            return false;
        }
        Inventory inventory = player.m_150109_(); // getInventory
        StorageCell cell = StorageCells.getCellInventory(stack, null);
        if (cell == null || inventory.m_36056_() != stack) { // getSelected
            return false;
        }
        if (!cell.getAvailableStacks().isEmpty()) {
            return false;
        }
        // Place (or drop) both products first, then consume the cell.
        inventory.m_150079_(new ItemStack(coreItem)); // placeItemBackInInventory
        inventory.m_150079_(new ItemStack(getHousingItem())); // placeItemBackInInventory
        inventory.m_6836_(inventory.f_35977_, ItemStack.f_41583_); // setItem(selected, EMPTY)
        return true;
    }

    private ItemLike getHousingItem() {
        return com.reiryoku_expansion.item.ReiryokuItems.REIRYOKU_CELL_HOUSING.get();
    }

    @Override
    public void m_7373_(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) { // appendHoverText
        ReiryokuCellHandler.INSTANCE.addCellInformationToTooltip(stack, lines);
    }
}
