package com.reiryoku_expansion.ae2;

import com.iwaliner.urushi.util.ElementUtils;
import com.iwaliner.urushi.util.interfaces.HasReiryokuItem;
import com.reiryoku_expansion.ReiryokuElement;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.GenericStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Makes Urushi Reiryoku items (element magatama etc.) usable as pattern
 * ingredients: the encoding terminal reads their contained Reiryoku as a
 * {@link ReiryokuKey} so auto-crafting can account for the required amount.
 */
public class ReiryokuContainerItemStrategy implements ContainerItemStrategy<ReiryokuKey, ItemStack> {

    @Override
    public GenericStack getContainedStack(ItemStack stack) {
        if (stack.m_41619_()) { // isEmpty
            return null;
        }
        if (stack.m_41720_() instanceof HasReiryokuItem hasReiryokuItem) { // getItem
            ReiryokuElement element = ReiryokuElement.fromUrushi(hasReiryokuItem.getElementType());
            if (element == null) {
                return null;
            }
            int amount = ElementUtils.getStoredReiryokuAmount(stack);
            if (amount <= 0) {
                return null;
            }
            return new GenericStack(ReiryokuKey.of(element), amount);
        }
        return null;
    }

    @Override
    public ItemStack findCarriedContext(Player player, AbstractContainerMenu menu) {
        return menu.m_142621_(); // getCarried
    }

    @Override
    public ItemStack findPlayerSlotContext(Player player, int slot) {
        return player.m_150109_().m_8020_(slot); // getInventory().getItem
    }

    @Override
    public long extract(ItemStack stack, ReiryokuKey what, long amount, Actionable mode) {
        if (stack.m_41619_() || !(stack.m_41720_() instanceof HasReiryokuItem hasReiryokuItem)) {
            return 0;
        }
        if (ReiryokuElement.fromUrushi(hasReiryokuItem.getElementType()) != what.getElement()) {
            return 0;
        }
        long toExtract = Math.min(amount, ElementUtils.getStoredReiryokuAmount(stack));
        if (toExtract > 0 && mode == Actionable.MODULATE) {
            ElementUtils.increaseStoredReiryokuAmount(stack, -(int) toExtract);
        }
        return toExtract;
    }

    @Override
    public long insert(ItemStack stack, ReiryokuKey what, long amount, Actionable mode) {
        if (stack.m_41619_() || !(stack.m_41720_() instanceof HasReiryokuItem hasReiryokuItem)) {
            return 0;
        }
        if (ReiryokuElement.fromUrushi(hasReiryokuItem.getElementType()) != what.getElement()) {
            return 0;
        }
        int current = ElementUtils.getStoredReiryokuAmount(stack);
        long toInsert = Math.min(amount, ElementUtils.getReiryokuCapacity(stack) - current);
        if (toInsert > 0 && mode == Actionable.MODULATE) {
            ElementUtils.setStoredReiryokuAmount(stack, current + (int) toInsert);
        }
        return toInsert;
    }

    @Override
    public void playFillSound(Player player, ReiryokuKey what) {
        // No dedicated Urushi sound is exposed; keep silent.
    }

    @Override
    public void playEmptySound(Player player, ReiryokuKey what) {
        // No dedicated Urushi sound is exposed; keep silent.
    }

    @Override
    public GenericStack getExtractableContent(ItemStack stack) {
        return getContainedStack(stack);
    }
}
