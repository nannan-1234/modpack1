package nannan.dev.multiblocks.phasechange;

import mekanism.api.IContentsListener;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 相变控制炉**结构件**方块实体的公共基类：外壳 / 控制器 / 阀门都继承它。
 *
 * <p>存在的理由（2026-09-28 实机反馈）：Mekanism 的 {@code TileEntityMekanism.openGui} 是
 * "拿**当前这块**方块去查 {@code AttributeGui}，再用**这块方块实体**去建容器"。
 * 之前容器只注册在控制器方块实体类型上，于是点到外壳 / 阀门 / 结构玻璃（玻璃会委托给主控方块）时，
 * 容器工厂因为"传进来的不是控制器方块实体"而返回 null，Mekanism 记一行 INFO 后拿 null 去
 * {@code NetworkHooks.openScreen} → NPE，表现为"右键没反应 / 交互坏掉"。</p>
 *
 * <p>现在把两个容器都注册到这个基类上，任何结构件都能建出容器；多方块数据本身通过
 * {@code getMultiblock()} 取（与 Mekanism 自己的 {@code addContainerTrackers} 完全一致，
 * 所以从哪一块打开看到的都是同一份数据）。</p>
 */
public abstract class PhaseChangePartTile extends TileEntityMultiblock<PhaseChangeMultiblockData> {

    protected PhaseChangePartTile(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    public PhaseChangeMultiblockData createMultiblock() {
        return new PhaseChangeMultiblockData(this);
    }

    @Override
    public MultiblockManager<PhaseChangeMultiblockData> getManager() {
        return PhaseChangeMultiblockData.manager;
    }

    /**
     * 把多方块数据里的热容量器暴露给邻居（与 Mekanism 锅炉外壳同款写法）：
     * 电阻加热器 / 燃料木加热器会主动调用 {@code simulateAdjacent} 把热推进来，
     * 我们不需要自己调 {@code simulateAdjacent}，因此仍然满足决策 14（本机不主动与邻居换热）。
     */
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return direction -> getMultiblock().getHeatCapacitors(direction);
    }

    /** 热量由多方块数据持有，不由单个方块实体持久化（与锅炉外壳一致）。 */
    @Override
    public boolean persists(SubstanceType type) {
        return type != SubstanceType.HEAT && super.persists(type);
    }
}
