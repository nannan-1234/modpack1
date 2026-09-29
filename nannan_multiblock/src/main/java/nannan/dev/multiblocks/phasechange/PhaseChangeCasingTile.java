package nannan.dev.multiblocks.phasechange;

import mekanism.api.providers.IBlockProvider;
import nannan.dev.multiblocks.registry.NMBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 相变控制炉外壳方块实体。
 *
 * <p>{@code TileEntityMultiblock} 负责结构探测与主控选举，{@code canBeMaster()} 默认为 true，
 * 所以任意一块外壳都能充当主控（与锅炉/储罐一致）。公共行为（多方块数据、热能力代理、持久化）
 * 都在 {@link PhaseChangePartTile} 里。</p>
 */
public class PhaseChangeCasingTile extends PhaseChangePartTile {

    public PhaseChangeCasingTile(BlockPos pos, BlockState state) {
        this(NMBlocks.CONTROL_FURNACE_CASING, pos, state);
    }

    public PhaseChangeCasingTile(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }
}
