package com.obanvilcompat;

import net.minecraftforge.fml.common.Mod;

/**
 * Minimal compat mod: makes OpenBlocks Reborn's auto anvil participate in the
 * Forge {@code AnvilUpdateEvent} pipeline, so event-driven anvil behaviors
 * (e.g. SlashBlade's refine / 精锻) work inside the machine.
 *
 * <p>The mixin config is registered through the {@code MixinConfigs} attribute
 * in {@code META-INF/MANIFEST.MF} (standard Forge 1.20.1 mechanism).</p>
 */
@Mod("openblocks_anvil_compat")
public final class OpenBlocksAnvilCompat {

    public OpenBlocksAnvilCompat() {
        // No runtime work needed; the mixin does everything.
    }
}
