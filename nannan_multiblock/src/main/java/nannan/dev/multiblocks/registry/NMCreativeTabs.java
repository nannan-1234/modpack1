package nannan.dev.multiblocks.registry;

import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.registration.impl.CreativeTabRegistryObject;
import nannan.dev.multiblocks.NannanMultiblocks;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * 创造模式物品栏页（本模组的所有方块都放进去）。
 *
 * <p><b>为什么必须有</b>：JEI 15 的物品清单是**从创造物品栏页收集**的——它的
 * {@code ItemStackListFactory.create} 只遍历 {@code CreativeModeTab.Type.CATEGORY} 的页面
 * （原版的"搜索"页是 {@code SEARCH} 类型，会被明确跳过），只有打开 JEI 的"显示隐藏物品"
 * 或原版的"管理员物品页"开关时，才会兜底扫描整张物品注册表。
 * 所以不在任何页面里的物品，在 JEI 里默认搜不到 —— 这正是之前搜不到本模组方块的原因。</p>
 *
 * <p>用 Mekanism 的 {@link CreativeTabDeferredRegister}：它封装了 1.20.1 的
 * {@code CreativeModeTab.Builder}（原版类，方法名在 SRG 里都被映射过），
 * 省掉一堆 {@code m_xxxxx_} 查表；{@code addToDisplay(BlockDeferredRegister, Output)}
 * 还能一次把整个方块注册器里的物品都加进去。</p>
 */
public final class NMCreativeTabs {

    private static final CreativeTabDeferredRegister TABS =
          new CreativeTabDeferredRegister(NannanMultiblocks.MODID);

    /** 页面图标用控制器方块；内容 = {@link NMBlocks#BLOCKS} 里的全部方块物品。 */
    public static final CreativeTabRegistryObject MAIN = TABS.registerMain(NMLang.CREATIVE_TAB,
          NMBlocks.CONTROL_FURNACE_CONTROLLER,
          // CreativeModeTab.Builder 是原版类，方法名在 SRG 里是 m_257501_(DisplayItemsGenerator)。
          builder -> builder.m_257501_((parameters, output) ->
                CreativeTabDeferredRegister.addToDisplay(NMBlocks.BLOCKS, output)));

    private NMCreativeTabs() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
