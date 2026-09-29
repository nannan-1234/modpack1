package nannan.dev.multiblocks.phasechange;

import mekanism.api.providers.IBlockProvider;
import nannan.dev.multiblocks.registry.NMBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 相变控制炉控制器方块实体。
 *
 * <p>与外壳同逻辑，区别在结构校验器里被判定为 {@code CasingType.OTHER}（不算框架，但允许出现在结构上），
 * 后续 GUI、速率滑块与停堆按钮都会挂在这个方块上。</p>
 */
public class PhaseChangeControllerTile extends PhaseChangePartTile {

    public PhaseChangeControllerTile(BlockPos pos, BlockState state) {
        this(NMBlocks.CONTROL_FURNACE_CONTROLLER, pos, state);
    }

    public PhaseChangeControllerTile(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }
}
