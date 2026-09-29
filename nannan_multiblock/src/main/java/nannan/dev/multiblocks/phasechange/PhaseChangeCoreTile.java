package nannan.dev.multiblocks.phasechange;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.tile.prefab.TileEntityInternalMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 相变控制炉内部核心方块。
 *
 * <p>放在外壳内部（属于内部方块而不是外壳），负责两件事（决策 12 与 A+B 缩放里的 B 项）：
 * ① 成形门槛——结构里至少要有 1 个核心；② 提高发热上限——每个核心 +10000 热/t。
 * 核心不参与热容计算（热容只看外壳块数），所以"堆核心"提高的是温度**上限**而不是惯性。</p>
 */
public class PhaseChangeCoreTile extends TileEntityInternalMultiblock {

    public PhaseChangeCoreTile(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }
}
