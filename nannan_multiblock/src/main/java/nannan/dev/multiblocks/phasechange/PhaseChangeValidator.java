package nannan.dev.multiblocks.phasechange;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.lib.multiblock.FormationProtocol.CasingType;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.util.WorldUtils;
import nannan.dev.multiblocks.registry.NMBlockTypes;
import nannan.dev.multiblocks.registry.NMLang;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 结构校验器：3..18 的空心长方体（边界由 {@link CuboidStructureValidator} 的无参构造给出）。
 *
 * <p>原型期外壳直接接受通用机械的「结构玻璃」（{@code mekanism:structural_glass}）——
 * 它本身就是 {@code IStructuralMultiblock}，可以给自定义 manager 当结构件，
 * 这样在自绘材质之前就能先搭出结构验证成形逻辑。</p>
 */
public class PhaseChangeValidator extends CuboidStructureValidator<PhaseChangeMultiblockData> {

    @Override
    protected CasingType getCasingType(BlockState state) {
        Block block = blockOf(state);
        if (BlockType.is(block, NMBlockTypes.CONTROL_FURNACE_CASING)) {
            return CasingType.FRAME;
        }
        if (BlockType.is(block, MekanismBlockTypes.STRUCTURAL_GLASS)) {
            return CasingType.FRAME;
        }
        if (BlockType.is(block, NMBlockTypes.CONTROL_FURNACE_VALVE)) {
            return CasingType.VALVE;
        }
        if (BlockType.is(block, NMBlockTypes.CONTROL_FURNACE_CONTROLLER)) {
            return CasingType.OTHER;
        }
        return CasingType.INVALID;
    }

    /** 内部允许空气，也允许本模组的核心方块（核心不参与外壳判定）。 */
    @Override
    protected boolean validateInner(BlockState state, Long2ObjectMap<ChunkAccess> chunkMap, BlockPos pos) {
        return super.validateInner(state, chunkMap, pos) || BlockType.is(blockOf(state), NMBlockTypes.CONTROL_FURNACE_CORE);
    }

    /**
     * 成形后的内部统计：数出核心数量交给数据类（决定 A+B 里的 B 项），并要求至少 1 个核心（决策 12）。
     */
    @Override
    public FormationResult postcheck(PhaseChangeMultiblockData data, Long2ObjectMap<ChunkAccess> chunkMap) {
        int cores = 0;
        for (BlockPos pos : data.internalLocations) {
            if (WorldUtils.getTileEntity(world, chunkMap, pos) instanceof PhaseChangeCoreTile) {
                cores++;
            }
        }
        if (cores <= 0) {
            return FormationResult.fail(NMLang.NEEDS_CORE);
        }
        data.setCoreCount(cores);
        return FormationResult.SUCCESS;
    }

    /**
     * 本模组直接用 javac 对本地 SRG 类库编译，所以调用原版方法必须写 SRG 名：
     * {@code BlockState.getBlock()} 在 SRG 里是 {@code m_60734_}。运行时 Forge 不会改写已经
     * 是 SRG 形状的引用，因此这样调用是安全的（与 openblocks_anvil_compat 的做法一致）。
     */
    private static Block blockOf(BlockState state) {
        return state.m_60734_();
    }
}
