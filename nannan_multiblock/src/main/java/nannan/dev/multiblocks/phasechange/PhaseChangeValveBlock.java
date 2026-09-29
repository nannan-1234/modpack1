package nannan.dev.multiblocks.phasechange;

import java.util.function.UnaryOperator;
import mekanism.common.content.blocktype.BlockTypeTile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * 阀门方块：比 {@link PhaseChangeBlock} 多一个 **`mode` 方块状态属性**，用来按阀门模式换外观。
 *
 * <p>为什么阀门要单独一个方块类：属性是"方块级"的，如果加在共用的 {@code PhaseChangeBlock} 上，
 * 外壳 / 控制器 / 核心也会带上这个没用的属性（F3 与 blockstate JSON 都会变脏）。
 * 这里只让阀门带属性，每个模式一套模型（强调色 + LED 颜色不同），世界里的阀门一眼能看出当前模式。</p>
 *
 * <p>这是照 Mekanism 自己的做法来的：锅炉阀门就是 `mode=` 属性 + 每模式一个模型
 * （`mekanism:blockstates/boiler_valve.json`）。方块状态的同步在
 * {@link PhaseChangeValveTile#setMode}（配置器切换时）里做。</p>
 */
public class PhaseChangeValveBlock extends PhaseChangeBlock<PhaseChangeValveTile> {

    /** EnumProperty.create 的 SRG 名是 m_61587_；枚举值名字见 ValveMode#getName()。 */
    public static final EnumProperty<ValveMode> MODE =
          EnumProperty.m_61587_("mode", ValveMode.class);

    public PhaseChangeValveBlock(BlockTypeTile<PhaseChangeValveTile> type,
          UnaryOperator<BlockBehaviour.Properties> properties) {
        super(type, properties);
    }

    @Override
    protected void m_7926_(StateDefinition.Builder<Block, BlockState> builder) {
        super.m_7926_(builder);
        // StateDefinition.Builder.add 的 SRG 名是 m_61104_。
        builder.m_61104_(MODE);
    }
}
