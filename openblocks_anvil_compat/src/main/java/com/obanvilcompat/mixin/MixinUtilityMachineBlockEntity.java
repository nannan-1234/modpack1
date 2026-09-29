package com.obanvilcompat.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import net.xuwu.openblocks_reborn.block.UtilityMachineBlock;
import net.xuwu.openblocks_reborn.blockentity.UtilityMachineBlockEntity;
import net.xuwu.openblocks_reborn.registry.ModFluids;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hooks the OpenBlocks Reborn auto anvil so it fires the same
 * {@link AnvilUpdateEvent} as a vanilla anvil does.
 *
 * <p>Why: SlashBlade's refine (精锻) is not a recipe; it is a listener on
 * {@code AnvilUpdateEvent}. OpenBlocks Reborn re-implements the vanilla anvil
 * logic in {@code UtilityMachineBlockEntity.calculateAnvilOperation} and never
 * fires the event, so the blade + proudsoul combination is invisible to it.
 * We fire the event before the machine's own logic and, if any listener
 * produced a result, settle it with the machine's own accounting:
 * level cost is paid as liquid XP from the internal tank, the left stack is
 * consumed, {@code materialCost} items are consumed from the right slot, and
 * the output goes to slot 2.</p>
 *
 * <p>Because the machine pays from an XP tank rather than a player, the event
 * is fired with a fake player whose level mirrors the tank's current liquid-XP
 * level count. This makes handlers (e.g. SlashBlade's refine loop) process as
 * many material units as the tank can pay for in one operation — a full stack
 * of 64 proudsouls costs 64 levels, so the anvil tank is enlarged to 100
 * levels (see {@code experienceCapacity} injector) to fit it.</p>
 *
 * <p>Mixin note: {@code @Shadow} members must be declared in the target class
 * itself. Mixin 0.8.5's lookup ({@code TargetClassContext.findAliasedField})
 * only scans the target class node and never traverses superclasses, so
 * inherited members (e.g. {@code BlockEntity.f_58857_}) cannot be shadowed.</p>
 */
@Mixin(UtilityMachineBlockEntity.class)
public abstract class MixinUtilityMachineBlockEntity {

    @Shadow
    @Final
    private ItemStackHandler inventory;

    @Shadow
    @Final
    private FluidTank experienceTank;

    @Shadow
    private String anvilItemName;

    /**
     * The stock auto anvil XP tank only holds 45 levels' worth of liquid XP,
     * which is not enough for a full-stack refine (64 materials × 1 level =
     * 64 levels). Enlarge the auto anvil tank to 100 levels.
     */
    @Inject(method = "experienceCapacity", at = @At("RETURN"), cancellable = true, remap = false)
    private static void obanvilcompat$enlargeAnvilXpTank(BlockState state, CallbackInfoReturnable<Integer> cir) {
        Block block = state.m_60734_();
        if (block instanceof UtilityMachineBlock machine && machine.kind() == UtilityMachineBlock.Kind.AUTO_ANVIL) {
            int hundredLevels = ModFluids.xpToFluid(ModFluids.experienceForLevel(100));
            if (cir.getReturnValue() < hundredLevels) {
                cir.setReturnValue(hundredLevels);
            }
        }
    }

    /**
     * Runs every machine tick (40 ticks) before the machine's own anvil logic.
     */
    @Inject(method = "repairItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void obanvilcompat$fireAnvilUpdateEvent(ServerLevel level, BlockPos pos, CallbackInfo ci) {
        ItemStack left = this.inventory.getStackInSlot(0);
        ItemStack right = this.inventory.getStackInSlot(1);
        ItemStack output = this.inventory.getStackInSlot(2);

        // Mirror the machine's own guards: need a left item and a free output slot.
        if (left.m_41619_() || right.m_41619_() || !output.m_41619_()) {
            return;
        }

        AnvilUpdateEvent event = this.fireAnvilUpdate(left, right, level);
        if (event == null || event.isCanceled() || event.getOutput().m_41619_() || event.getCost() <= 0) {
            return;
        }

        int levelCost = event.getCost();
        int materialCost = event.getMaterialCost();
        int fluidCost = ModFluids.xpToFluid(ModFluids.experienceForLevel(levelCost));
        if (this.experienceTank.getFluidAmount() < fluidCost) {
            return;
        }

        this.experienceTank.drain(fluidCost, FluidAction.EXECUTE);
        this.inventory.extractItem(0, 1, false);
        if (materialCost > 0) {
            this.inventory.extractItem(1, materialCost, false);
        }
        this.inventory.setStackInSlot(2, event.getOutput());
        this.anvilItemName = null;
        level.m_5594_(null, pos, SoundEvents.f_11671_, SoundSource.BLOCKS, 0.3F, 1.0F);
        ci.cancel();
    }

    /**
     * Makes the machine GUI show the event-computed level cost for operations
     * such as SlashBlade refine. The stock {@code getSelectedMachineCost} only
     * knows the machine's own vanilla anvil calculation, so it would display 0
     * for event-driven results.
     *
     * <p>The machine's level is fetched through a cast to {@link BlockEntity}
     * instead of a {@code @Shadow} because Mixin 0.8.5 cannot shadow inherited
     * members (see class Javadoc).</p>
     */
    @Inject(method = "getSelectedMachineCost", at = @At("HEAD"), cancellable = true, remap = false)
    private void obanvilcompat$showAnvilUpdateCost(CallbackInfoReturnable<Integer> cir) {
        ItemStack left = this.inventory.getStackInSlot(0);
        ItemStack right = this.inventory.getStackInSlot(1);
        if (left.m_41619_() || right.m_41619_()) {
            return;
        }
        Level level = ((BlockEntity) (Object) this).m_58904_();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        AnvilUpdateEvent event = this.fireAnvilUpdate(left, right, serverLevel);
        if (event == null || event.isCanceled() || event.getOutput().m_41619_() || event.getCost() <= 0) {
            return;
        }
        cir.setReturnValue(event.getCost());
    }

    private AnvilUpdateEvent fireAnvilUpdate(ItemStack left, ItemStack right, ServerLevel level) {
        FakePlayer fake = FakePlayerFactory.getMinecraft(level);
        int tankLevels = ModFluids.levelForExperience(ModFluids.fluidToXp(this.experienceTank.getFluidAmount()));
        int previousLevel = fake.f_36078_;
        fake.f_36078_ = tankLevels;
        try {
            AnvilUpdateEvent event = new AnvilUpdateEvent(left, right, this.anvilItemName, 0, fake);
            MinecraftForge.EVENT_BUS.post(event);
            return event;
        } finally {
            fake.f_36078_ = previousLevel;
        }
    }
}
