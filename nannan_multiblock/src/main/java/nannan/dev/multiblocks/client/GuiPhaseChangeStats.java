package nannan.dev.multiblocks.client;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.bar.GuiHorizontalRateBar;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import nannan.dev.multiblocks.client.element.GuiPageTab;
import nannan.dev.multiblocks.network.NMNetwork;
import nannan.dev.multiblocks.phasechange.PhaseChangeMultiblockData;
import nannan.dev.multiblocks.phasechange.PhaseChangePartTile;
import nannan.dev.multiblocks.registry.NMLang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 相变控制炉统计子页（设计稿 §1.10，195 × 212，没有物品槽与玩家背包）。
 *
 * <p>放的都是"不会每分钟变"的参数：产热统计 / 燃料统计 / 反应统计 + 当前燃烧速率条。
 * 主页面只留玩家每分钟要看的读数，这样温度曲线才有高度。</p>
 */
public class GuiPhaseChangeStats extends GuiMekanismTile<PhaseChangePartTile, EmptyTileContainer<PhaseChangePartTile>> {

    private static final int WINDOW_WIDTH = 195;
    private static final int WINDOW_HEIGHT = 212;
    private static final int INK = 0x404040;
    private static final int HEADING = 0x232323;
    private static final double MB_PER_ITEM = 90.0D;
    private static final double HEAT_PER_MB = 500_000.0D;
    private static final double CASING_HEAT_CAPACITY = 50.0D;
    private static final double BASE_HEAT_PER_SHELL = 2000.0D;
    private static final double MAX_HEAT_PER_CORE = 10_000.0D;

    public GuiPhaseChangeStats(EmptyTileContainer<PhaseChangePartTile> container, Inventory inv, Component title) {
        super(container, inv, title);
        f_97726_ = WINDOW_WIDTH;
        f_97727_ = WINDOW_HEIGHT;
        f_97729_ = 5;
        // 统计页没有槽位，但保持与主页面一致的开关（Mekanism 的虚拟槽位渲染）。
        dynamicSlots = true;
    }

    private PhaseChangeMultiblockData data() {
        return tile.getMultiblock();
    }

    @Override
    public int getXSize() {
        return f_97726_;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiPageTab(this, tile, NMNetwork.PAGE_MAIN, NMLang.GUI_TAB_MAIN, -26, 12));
        // Mekanism 自带的横向速率条：宽度固定为 GUI 宽度 - 12（= 183），正好是设计稿的尺寸。
        addRenderableWidget(new GuiHorizontalRateBar(this, () -> data().burnRateLimit, 6, 170));
    }

    @Override
    protected void drawForegroundText(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        PhaseChangeMultiblockData data = data();
        double heatCapacity = CASING_HEAT_CAPACITY * data.shellCount;
        double maxHeat = BASE_HEAT_PER_SHELL * data.shellCount + MAX_HEAT_PER_CORE * data.coreCount;
        double maxBurn = maxHeat / HEAT_PER_MB;
        double burn = data.lastBurnRate;

        drawString(guiGraphics, NMLang.GUI_SECTION_HEAT.translate(), 6, 18, HEADING);
        drawString(guiGraphics, NMLang.GUI_SHELLS_CORES.translate(number(data.shellCount), number(data.coreCount)), 6, 28, INK);
        drawString(guiGraphics, NMLang.GUI_HEAT_CAPACITY.translate(number(heatCapacity)), 6, 38, INK);
        drawString(guiGraphics, NMLang.GUI_MAX_HEAT.translate(number(maxHeat)), 6, 48, INK);
        drawString(guiGraphics, NMLang.GUI_CONDUCTIVITY.translate(), 6, 58, INK);

        drawString(guiGraphics, NMLang.GUI_SECTION_FUEL.translate(), 6, 72, HEADING);
        drawString(guiGraphics, NMLang.GUI_MAX_BURN.translate(format(maxBurn, 2)), 6, 82, INK);
        drawString(guiGraphics, NMLang.GUI_RATE_LIMIT.translate(format(burn, 2),
              Math.round(data.burnRateLimit * 100) + "%"), 6, 92, INK);

        drawString(guiGraphics, NMLang.GUI_SECTION_REACTION.translate(), 6, 106, HEADING);
        // 反应通量用"锭/t"（决策 7：1 锭 = 90 mB），下面一行补上对应的燃料消耗 mB/t
        drawString(guiGraphics, NMLang.GUI_FLUX.translate().m_7220_(
              NMLang.GUI_FLUX_VALUE.translate(format(burn / MB_PER_ITEM, 3))), 6, 116, INK);
        drawString(guiGraphics, NMLang.GUI_FLUX_ITEM.translate(format(burn, 2)), 6, 126, INK);
        drawString(guiGraphics, NMLang.GUI_HEAT_PER_MB.translate(number(HEAT_PER_MB)), 6, 136, INK);
        drawString(guiGraphics, NMLang.GUI_STABLE.translate().m_7220_(
              NMLang.GUI_STABLE_VALUE.translate(data.stableTicks)), 6, 146, INK);

        drawString(guiGraphics, NMLang.GUI_CURRENT_BURN.translate(format(burn, 2)), 6, 160, INK);
        drawString(guiGraphics, NMLang.GUI_TEMPERATURE_REFERENCE.translate(), 6, 186, INK);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    private static String format(double value, int digits) {
        return String.format("%." + digits + "f", value);
    }

    /** 千分位整数（Mekanism 自己的统计页也是这种写法）。 */
    private static String number(double value) {
        return String.format("%,.0f", value);
    }
}
