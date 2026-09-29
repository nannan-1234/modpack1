package nannan.dev.multiblocks.phasechange;

import mekanism.common.inventory.container.tile.MekanismTileContainer;
import nannan.dev.multiblocks.registry.NMContainers;
import net.minecraft.world.entity.player.Inventory;

/**
 * 相变控制炉**主页面**容器（设计稿 §1.10：195 × 278）。
 *
 * <p>只做两件事：把多方块数据里的两个物品槽包成容器槽位（坐标直接用槽位自带的 x/y，
 * 所以 GUI 元素与逻辑槽位天然对齐），以及把玩家背包整体下移到设计稿的位置。
 * 数值同步不用在这里写：{@code TileEntityMultiblock.addContainerTrackers} 已经调用
 * {@code SyncMapper.setup(container, 多方块数据类, ...)}，数据类上带 {@code @ContainerSync} 的字段会自动下发。</p>
 */
public class PhaseChangeContainer extends MekanismTileContainer<PhaseChangePartTile> {

    public PhaseChangeContainer(int windowId, Inventory inv, PhaseChangePartTile tile) {
        super(NMContainers.CONTROL_FURNACE, windowId, inv, tile);
    }

    @Override
    protected void addSlots() {
        super.addSlots();
        // 多方块的数据在成形前也存在（defaultMultiblock），所以这里永远拿得到两个槽位。
        PhaseChangeMultiblockData data = tile.getMultiblock();
        // 原版 AbstractContainerMenu.addSlot(Slot) 在 SRG 里叫 m_38897_；MekanismContainer 复写了它，
        // 会把 InventoryContainerSlot 另外登记进 inventoryContainerSlots。
        m_38897_(data.materialInputSlot.createContainerSlot());
        m_38897_(data.productOutputSlot.createContainerSlot());
    }

    /** 玩家背包第一行在 y=193（设计稿里曲线下方）。 */
    @Override
    protected int getInventoryYOffset() {
        return 193;
    }

    /** 195 宽的窗口里把 9 列背包居中：x=17。 */
    @Override
    protected int getInventoryXOffset() {
        return 17;
    }
}
