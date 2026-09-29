package com.reiryoku_expansion.ae2;

import java.util.concurrent.ConcurrentHashMap;

import com.reiryoku_expansion.ReiryokuElement;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * AE2 key for one element of Reiryoku. Instances are cached per element so
 * identity equality (which AEKey relies on by default) stays correct even
 * after network / NBT round trips.
 */
public final class ReiryokuKey extends AEKey {

    private static final ConcurrentHashMap<ReiryokuElement, ReiryokuKey> CACHE = new ConcurrentHashMap<>();

    private final ReiryokuElement element;

    private ReiryokuKey(ReiryokuElement element) {
        this.element = element;
    }

    public static ReiryokuKey of(ReiryokuElement element) {
        return CACHE.computeIfAbsent(element, ReiryokuKey::new);
    }

    public ReiryokuElement getElement() {
        return element;
    }

    @Override
    public AEKeyType getType() {
        return ReiryokuKeyType.INSTANCE;
    }

    @Override
    public AEKey dropSecondary() {
        return this;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.m_128359_("e", element.getDataId()); // putString
        return tag;
    }

    @Override
    public Object getPrimaryKey() {
        return element;
    }

    @Override
    public ResourceLocation getId() {
        return element.getId();
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buffer) {
        buffer.m_130070_(element.getDataId()); // writeUtf
    }

    @Override
    public void addDrops(long amount, java.util.List<ItemStack> drops, Level level, BlockPos pos) {
        // Reiryoku cannot be dropped as an item; losing it silently would be bad,
        // but cell disassembly is only allowed for empty cells, so nothing to do.
    }

    @Override
    protected Component computeDisplayName() {
        return Component.m_237115_("key.reiryoku_expansion." + element.getLangKey()); // translatable
    }
}
