package com.reiryoku_expansion.ae2;

import com.iwaliner.urushi.util.interfaces.ReiryokuStorable;
import com.reiryoku_expansion.ReiryokuElement;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Shared lookup helpers for Urushi's {@link ReiryokuStorable} block entities.
 */
public final class ReiryokuStorables {

    private ReiryokuStorables() {
    }

    /** Returns the Reiryoku storable at {@code pos}, or null. */
    public static ReiryokuStorable find(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.m_7702_(pos); // getBlockEntity
        if (be instanceof ReiryokuStorable storable) {
            return storable;
        }
        return null;
    }

    /** Element this block actually stores (from its ElementBlock), or null. */
    public static ReiryokuElement storedElement(ReiryokuStorable storable) {
        return ReiryokuElement.fromUrushi(storable.getStoredElementType());
    }
}
