package com.reiryoku_expansion.ae2;

import com.iwaliner.urushi.util.interfaces.ReiryokuStorable;
import com.reiryoku_expansion.ReiryokuElement;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.core.localization.GuiText;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/**
 * Storage bus strategy: exposes an adjacent Urushi Reiryoku block as an ME
 * storage of its single element type.
 */
public class ReiryokuExternalStorageStrategy implements ExternalStorageStrategy {

    private final ServerLevel level;
    private final BlockPos pos;

    public ReiryokuExternalStorageStrategy(ServerLevel level, BlockPos pos, Direction fromSide) {
        this.level = level;
        this.pos = pos;
    }

    @Override
    public MEStorage createWrapper(boolean extractableOnly, Runnable injectOrExtractCallback) {
        ReiryokuStorable storable = ReiryokuStorables.find(level, pos);
        if (storable == null) {
            return null;
        }
        ReiryokuElement element = ReiryokuStorables.storedElement(storable);
        if (element == null) {
            return null;
        }
        return new ReiryokuStorageAdapter(storable, element, extractableOnly, injectOrExtractCallback);
    }

    /**
     * Direct MEStorage view over one Urushi Reiryoku block.
     */
    private static final class ReiryokuStorageAdapter implements MEStorage {

        private final ReiryokuStorable storable;
        private final ReiryokuElement element;
        private final boolean extractableOnly;
        private final Runnable injectOrExtractCallback;

        private ReiryokuStorageAdapter(ReiryokuStorable storable, ReiryokuElement element, boolean extractableOnly,
                                       Runnable injectOrExtractCallback) {
            this.storable = storable;
            this.element = element;
            this.extractableOnly = extractableOnly;
            this.injectOrExtractCallback = injectOrExtractCallback;
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (!(what instanceof ReiryokuKey key) || key.getElement() != element) {
                return 0;
            }
            // A storage bus configured as extract-only must never write into the
            // external block. Return 0 (= nothing inserted) so the caller keeps
            // the full amount; nothing has been modified at this point.
            if (extractableOnly) {
                return 0;
            }
            int free = storable.getReiryokuCapacity() - storable.getStoredReiryoku();
            long toAdd = Math.min(free, amount);
            if (toAdd <= 0) {
                return 0;
            }
            if (mode == Actionable.MODULATE) {
                storable.addStoredReiryoku((int) toAdd);
                storable.markUpdated();
                injectOrExtractCallback.run();
            }
            return toAdd;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (!(what instanceof ReiryokuKey key) || key.getElement() != element) {
                return 0;
            }
            long toExtract = Math.min(storable.getStoredReiryoku(), amount);
            if (toExtract <= 0) {
                return 0;
            }
            if (mode == Actionable.MODULATE) {
                storable.decreaseStoredReiryoku((int) toExtract);
                storable.markUpdated();
                injectOrExtractCallback.run();
            }
            return toExtract;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            int stored = storable.getStoredReiryoku();
            if (stored > 0) {
                out.add(ReiryokuKey.of(element), stored);
            }
        }

        @Override
        public Component getDescription() {
            return GuiText.ExternalStorage.text(ReiryokuKeyType.INSTANCE.getDescription());
        }
    }
}
