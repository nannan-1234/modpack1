package nannan.dev.multiblocks.phasechange;

import java.util.function.UnaryOperator;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.block.prefab.BlockBasicMultiblock;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 相变控制炉的方块基类。
 *
 * <p>比 {@link BlockBasicMultiblock} 多做一件事：</p>
 * <ul>
 *   <li><b>配置器（Mekanism 的扳手）处于配置模式时右键阀门</b>：循环切换阀门模式
 *       （原料 / 燃料 / 冷却剂 / 产物 / 冷却剂输出）。这是用户指定的触发方式——早期版本用的
 *       "潜行 + 空手"太容易误触，配置类操作应该跟 Mekanism 一样由配置器发起；聊天栏读数已按用户要求移除
 *       （温度 / 罐量 / 阀门模式在 GUI 里都有）。</li>
 * </ul>
 */
public class PhaseChangeBlock<TILE extends TileEntityMekanism> extends BlockBasicMultiblock<TILE> {

    public PhaseChangeBlock(BlockTypeTile<TILE> type, UnaryOperator<BlockBehaviour.Properties> properties) {
        super(type, properties);
    }

    @Override
    public InteractionResult m_6227_(BlockState state, Level level, BlockPos pos, Player player,
          InteractionHand hand, BlockHitResult hit) {
        // 原版成员在 SRG 里的名字：Level.isClientSide = f_46443_、Player.getItemInHand = m_21120_、
        // Player.displayClientMessage = m_5661_、Component.literal = m_237113_、
        // MutableComponent.append = m_7220_。
        ItemStack heldItem = player.m_21120_(hand);

        // ① 阀门模式切换：**拿着配置器（Mekanism 的扳手）并且处于配置模式**才能改（用户 2026-09-28 指定）。
        //    Forge 的交互顺序是"先方块后物品"，所以这里返回 SUCCESS 就能拦住配置器自己的 useOn，
        //    不会同时触发 Mekanism 的侧面配置逻辑。客户端也返回 SUCCESS，保证两侧判定一致。
        if (heldItem.m_41720_() instanceof ItemConfigurator configurator
              && configurator.getMode(heldItem).isConfigurating()) {
            if (!level.f_46443_ && WorldUtils.getTileEntity(level, pos) instanceof PhaseChangeValveTile valve) {
                ValveMode mode = valve.cycleMode();
                // 只回显"刚切到哪个模式"这一条；完整读数（温度 / 罐量…）已经移到 GUI，聊天栏不再刷状态。
                MutableComponent message = Component.m_237113_("阀门模式：");
                message.m_7220_(mode.getLangEntry().translateColored(mode.getColor()));
                player.m_5661_(message, false);
            }
            return InteractionResult.SUCCESS;
        }

        // ② 其余情况交给 Mekanism：空手右键 = 打开 GUI（AttributeGui → TileEntityMultiblock.onActivate），
        //    配置器的其它档位 = Mekanism 自己的侧面配置 / 拆装逻辑。
        return super.m_6227_(state, level, pos, player, hand, hit);
    }
}
