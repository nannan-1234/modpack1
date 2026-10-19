package com.reiryoku_expansion.ae2;

import com.reiryoku_expansion.ReiryokuElement;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * The Reiryoku key type: 1 byte stores 1 Reiryoku, and each import/export bus
 * operation moves exactly 1 Reiryoku (acceleration cards multiply the number
 * of operations per work cycle through AE2's normal upgrade logic).
 */
public final class ReiryokuKeyType extends AEKeyType {

    public static final ReiryokuKeyType INSTANCE = new ReiryokuKeyType();

    private ReiryokuKeyType() {
        super(new ResourceLocation("reiryoku_expansion", "reiryoku"),
                ReiryokuKey.class,
                Component.translatable("key.reiryoku_expansion.reiryoku")); // translatable
    }

    @Override
    public AEKey readFromPacket(FriendlyByteBuf buffer) {
        ReiryokuElement element = ReiryokuElement.byDataId(buffer.readUtf()); // readUtf
        return element == null ? null : ReiryokuKey.of(element);
    }

    @Override
    public AEKey loadKeyFromTag(CompoundTag tag) {
        Tag raw = tag.get("e"); // get
        if (raw instanceof StringTag stringTag) {
            ReiryokuElement element = ReiryokuElement.byDataId(stringTag.getAsString()); // getAsString
            return element == null ? null : ReiryokuKey.of(element);
        }
        if (raw instanceof IntTag) {
            // Legacy pre-1.2.1 format: the element was stored as an enum ordinal.
            ReiryokuElement element = ReiryokuElement.byLegacyOrdinal(tag.getInt("e")); // getInt
            return element == null ? null : ReiryokuKey.of(element);
        }
        return null;
    }

    @Override
    public int getAmountPerOperation() {
        return 1;
    }

    @Override
    public int getAmountPerByte() {
        return 1;
    }

    @Override
    public String getUnitSymbol() {
        return "RY";
    }
}
