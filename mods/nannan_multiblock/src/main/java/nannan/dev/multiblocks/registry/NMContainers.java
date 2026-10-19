package nannan.dev.multiblocks.registry;

import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import nannan.dev.multiblocks.NannanMultiblocks;
import nannan.dev.multiblocks.phasechange.PhaseChangeContainer;
import nannan.dev.multiblocks.phasechange.PhaseChangePartTile;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * 容器（MenuType）注册：相变控制炉有**两个页面**，各自一个容器类型。
 *
 * <p>与裂变反应堆一样，"主页面 / 统计页"是两个独立的容器：服务端换页面时打开另一个容器，
 * 客户端各绑一个 {@code Screen}。统计页没有物品槽与玩家背包，直接用 Mekanism 的
 * {@code EmptyTileContainer}（{@code registerEmpty}）。</p>
 *
 * <p><b>注册的方块实体类型必须是"结构件基类"</b>（{@link PhaseChangePartTile}）：Mekanism 的
 * {@code openGui} 用"被点的那一块"来查 GUI 属性并建容器，注册成控制器专用类型会导致点到外壳 /
 * 阀门 / 结构玻璃时容器工厂返回 null（Mekanism 只记一行 INFO），随后 {@code NetworkHooks.openScreen}
 * 收到 null 直接 NPE —— 这正是 2026-09-28 实机反馈里"右键外壳没反应"的原因。</p>
 */
public final class NMContainers {

    public static final ContainerTypeDeferredRegister CONTAINERS =
          new ContainerTypeDeferredRegister(NannanMultiblocks.MODID);

    /** 主页面：仪表 + 内屏 + 两个物品槽 + 速率滑块 + 温度条 / 曲线 + 玩家背包。 */
    public static final ContainerTypeRegistryObject<PhaseChangeContainer> CONTROL_FURNACE =
          CONTAINERS.register("control_furnace", PhaseChangePartTile.class,
                (windowId, inv, tile) -> new PhaseChangeContainer(windowId, inv, tile));

    /** 统计页：只有文字读数与两条读数条。 */
    public static final ContainerTypeRegistryObject<EmptyTileContainer<PhaseChangePartTile>>
          CONTROL_FURNACE_STATS = CONTAINERS.registerEmpty("control_furnace_stats", PhaseChangePartTile.class);

    private NMContainers() {
    }

    public static void register(IEventBus modBus) {
        CONTAINERS.register(modBus);
    }
}
