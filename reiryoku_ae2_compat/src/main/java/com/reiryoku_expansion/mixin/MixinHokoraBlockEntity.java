package com.reiryoku_expansion.mixin;

import com.iwaliner.urushi.blockentity.HokoraBlockEntity;
import com.iwaliner.urushi.util.ElementType;
import com.reiryoku_expansion.hokora.HokoraFuelConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes the Hokora (神龛) item → Reiryoku conversion data-driven instead of
 * hardcoded. The fuel item, the amount per conversion and the cooldown are
 * configured through {@code data/<namespace>/reiryoku_hokora/*.json} and can
 * be changed with {@code /reload}.
 *
 * <p>All three target methods belong to Urushi (not obfuscated), so the
 * injections use {@code remap = false} and no refmap is needed.</p>
 */
@Mixin(HokoraBlockEntity.class)
public abstract class MixinHokoraBlockEntity {

    @Inject(method = "getFuel", at = @At("HEAD"), cancellable = true, remap = false)
    private static void reiryokuexpansion$configuredFuel(ElementType element, CallbackInfoReturnable<Item> cir) {
        Item item = HokoraFuelConfig.getFuel(element);
        if (item != null) {
            cir.setReturnValue(item);
        }
    }

    @Inject(method = "getAddAmount", at = @At("HEAD"), cancellable = true, remap = false)
    private void reiryokuexpansion$configuredAmount(CallbackInfoReturnable<Integer> cir) {
        ElementType element = ((HokoraBlockEntity) (Object) this).getStoredElementType();
        Integer amount = HokoraFuelConfig.getReiryokuAmount(element);
        if (amount != null) {
            cir.setReturnValue(amount);
        }
    }

    /**
     * Replaces the hardcoded 100-tick cooldown in {@code tick}. The handler
     * receives the original constant plus the static tick method's arguments,
     * so it can look up the config for the ticking hokora's element.
     */
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 100), remap = false)
    private static int reiryokuexpansion$configuredCooldown(int original, Level level, BlockPos pos, BlockState state,
                                                      HokoraBlockEntity hokora) {
        Integer cooldown = HokoraFuelConfig.getCooldown(hokora.getStoredElementType());
        return cooldown == null ? original : cooldown;
    }

    /**
     * The four fuel gates below all do {@code stack.getItem() == getFuel(element)}.
     * Each method contains exactly one {@code ItemStack.getItem()} call, so we
     * redirect it: when the element is configured, the item comparison is made
     * against the configured ingredient (which supports tags / multiple items)
     * by returning the configured first item whenever the stack is accepted.
     * Unconfigured elements keep Urushi's original behavior.
     */
    @Redirect(method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;m_41720_()Lnet/minecraft/world/item/Item;"),
            remap = false)
    private static Item reiryokuexpansion$tickFuelItem(ItemStack stack, Level level, BlockPos pos, BlockState state,
                                                       HokoraBlockEntity hokora) {
        return resolveFuelItem(hokora, stack);
    }

    @Redirect(method = "m_7013_",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;m_41720_()Lnet/minecraft/world/item/Item;"),
            remap = false)
    private Item reiryokuexpansion$canPlaceFuelItem(ItemStack stack, int slot, ItemStack stackArg) {
        return resolveFuelItem((HokoraBlockEntity) (Object) this, stack);
    }

    @Redirect(method = "m_7155_",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;m_41720_()Lnet/minecraft/world/item/Item;"),
            remap = false)
    private Item reiryokuexpansion$canPlaceThroughFaceFuelItem(ItemStack stack, int slot, ItemStack stackArg,
                                                               Direction direction) {
        return resolveFuelItem((HokoraBlockEntity) (Object) this, stack);
    }

    @Redirect(method = "m_6836_",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;m_41720_()Lnet/minecraft/world/item/Item;"),
            remap = false)
    private Item reiryokuexpansion$setItemFuelItem(ItemStack stack, int slot, ItemStack stackArg) {
        return resolveFuelItem((HokoraBlockEntity) (Object) this, stack);
    }

    private static Item resolveFuelItem(HokoraBlockEntity hokora, ItemStack stack) {
        ElementType element = hokora.getStoredElementType();
        if (element != null && HokoraFuelConfig.isConfigured(element) && HokoraFuelConfig.isFuel(element, stack)) {
            return HokoraFuelConfig.getFuel(element);
        }
        return stack.m_41720_();
    }
}
