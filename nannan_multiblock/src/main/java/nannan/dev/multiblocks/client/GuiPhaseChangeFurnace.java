package nannan.dev.multiblocks.client;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiHybridGauge;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import nannan.dev.multiblocks.client.element.GuiPageTab;
import nannan.dev.multiblocks.client.element.GuiRateSlider;
import nannan.dev.multiblocks.client.element.GuiTemperatureBar;
import nannan.dev.multiblocks.client.element.GuiTemperatureCurve;
import nannan.dev.multiblocks.network.NMNetwork;
import nannan.dev.multiblocks.phasechange.PhaseChangeContainer;
import nannan.dev.multiblocks.phasechange.PhaseChangeMultiblockData;
import nannan.dev.multiblocks.phasechange.PhaseChangePartTile;
import nannan.dev.multiblocks.registry.NMLang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;

/**
 * 相变控制炉主页面（设计稿《multiblock_design.md》§1.10，方案 A：滑块在主页面）。
 *
 * <p>窗口 195 × 278：三个竖直储罐 + 顶部中间 5 行内屏 + 两个物品槽 + 速率滑块 + 温度条 +
 * 温度曲线 + 玩家背包。左侧挂"统计"标签换页。</p>
 *
 * <p>注意：所有元素坐标都是**窗口相对坐标**，与设计稿里的像素坐标一一对应；
 * 元素的渲染坐标要用 {@code relativeX/relativeY}（Mekanism 在背景/前台两遍渲染前已经平移过 pose）。</p>
 */
public class GuiPhaseChangeFurnace extends GuiMekanismTile<PhaseChangePartTile, PhaseChangeContainer> {

    private static final int WINDOW_WIDTH = 195;
    private static final int WINDOW_HEIGHT = 278;
    private static final int INK = 0x404040;
    /** 1 锭 = 90 mB（决策 7），用来把燃烧速率换算成"锭/t"的反应通量。 */
    private static final double MB_PER_ITEM = 90.0D;

    private GuiTemperatureCurve curve;
    /** 温度条与曲线共用的"当前配方温度窗口"状态（每 tick 从同步字段刷新）。 */
    private final RecipeWindow recipeWindow = new RecipeWindow();

    public GuiPhaseChangeFurnace(PhaseChangeContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        // 原版 AbstractContainerScreen 的布局字段：imageWidth / imageHeight / titleLabelY /
        // inventoryLabelX / inventoryLabelY（SRG 名见裂变反应堆的反编译：它也是这么改的）。
        f_97726_ = WINDOW_WIDTH;
        f_97727_ = WINDOW_HEIGHT;
        f_97729_ = 5;
        f_97730_ = 6;
        f_97731_ = 185;
        // 打开 Mekanism 的"虚拟槽位"渲染：GuiMekanism.addGuiElements() 会在 dynamicSlots 为 true 时
        // 遍历容器的所有槽位（**包括玩家背包**）各加一个 GuiSlot，槽位边框就是它画的。
        // 不开的话玩家背包那一格格子会没有边框（实机反馈里的"物品栏很奇怪"）。
        dynamicSlots = true;
    }

    private PhaseChangeMultiblockData data() {
        return tile.getMultiblock();
    }

    /**
     * {@code IFancyFontRenderer} 里的 {@code getXSize()} 是抽象方法：原版 {@code AbstractContainerScreen}
     * 的同名方法在 SRG 里被改名了，接口因此不会被自动满足（Mekanism 自己用 Mojang 映射编译，没这个问题），
     * 这里显式转发到 {@code imageWidth}（f_97726_）。
     */
    @Override
    public int getXSize() {
        return f_97726_;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiPageTab(this, tile, NMNetwork.PAGE_STATS, NMLang.GUI_TAB_STATS, -26, 12));

        // 三个竖直储罐（标准仪表 18 × 60，与裂变反应堆同款）：燃料 / 冷却剂（水钠共用一个混合仪表）/ 产物
        addRenderableWidget(new GuiGasGauge(() -> data().fuelTank, () -> List.of(data().fuelTank),
              GaugeType.STANDARD, this, 6, 13).setLabel(NMLang.GUI_FUEL.translate()));
        addRenderableWidget(new GuiHybridGauge(() -> data().gasCoolantTank, () -> List.of(data().gasCoolantTank),
              () -> data().fluidCoolantTank, () -> List.of(data().fluidCoolantTank),
              GaugeType.STANDARD, this, 25, 13).setLabel(NMLang.GUI_COOLANT.translate()));
        addRenderableWidget(new GuiGasGauge(() -> data().heatedCoolantTank, () -> List.of(data().heatedCoolantTank),
              GaugeType.STANDARD, this, 173, 13).setLabel(NMLang.GUI_PRODUCT.translate()));

        // 顶部中间的内屏：状态 / 燃烧速率 / 反应通量 / 温度 / 稳定计时
        addRenderableWidget(new GuiInnerScreen(this, 45, 17, 124, 56, this::getStatusText).spacing(2));

        // 两个物品槽**不用手写**：dynamicSlots = true 时 Mekanism 已经按容器槽位（含 INPUT / OUTPUT 类型）
        // 自动画好了，重复添加只会画两遍。

        // 速率滑块（0..100），拖动时发包
        addRenderableWidget(new GuiRateSlider(this, 68, 85, 121, 22, this::burnRatePercent, this::setBurnRatePercent));

        // 温度条（刻度：沸点 373 K / 停堆 1500 K）与温度曲线；两者都会画"当前配方窗口"的色带
        addRenderableWidget(new GuiTemperatureBar(this, 30, 111, 159, 12, () -> data().temperatureK, recipeWindow));
        curve = addRenderableWidget(new GuiTemperatureCurve(this, 6, 137, 183, 40, () -> data().temperatureK, recipeWindow));
    }

    @Override
    public void m_181908_() {
        super.m_181908_();
        PhaseChangeMultiblockData data = data();
        recipeWindow.update(data.activeRecipeMinTemp, data.activeRecipeMaxTemp, data.activeRecipeInWindow);
        if (curve != null) {
            curve.addSample();
        }
    }

    @Override
    protected void drawForegroundText(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        // 储罐标签（设计稿里在仪表下方一行）
        drawGaugeLabel(guiGraphics, 6, NMLang.GUI_FUEL.translate());
        drawGaugeLabel(guiGraphics, 25, coolantLabel());
        drawGaugeLabel(guiGraphics, 173, productLabel());
        // 输入槽 → 输出槽 的小箭头
        drawArrow(guiGraphics, 27, 89);
        // 温度条与曲线的标签行
        drawString(guiGraphics, NMLang.GUI_TEMPERATURE.translate(), 6, 113, INK);
        drawString(guiGraphics, NMLang.GUI_CURVE.translate(), 6, 128, INK);
        Component recipe = recipeLine();
        drawString(guiGraphics, recipe, WINDOW_WIDTH - 6 - getStringWidth(recipe), 128, INK);
        // Mekanism 接管了原版的 renderLabels，玩家背包的标题要自己画
        drawString(guiGraphics, NMLang.GUI_INVENTORY.translate(), 6, 185, INK);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    private List<Component> getStatusText() {
        PhaseChangeMultiblockData data = data();
        int percent = burnRatePercent();
        boolean scrammed = data.isScrammed();
        MutableComponent status = scrammed
              ? NMLang.GUI_STATUS_SCRAMMED.translateColored(EnumColor.RED)
              : (percent <= 0 ? NMLang.GUI_STATUS_IDLE.translateColored(EnumColor.GRAY)
              : (data.lastBurnRate <= 0 ? NMLang.GUI_STATUS_NO_FUEL.translateColored(EnumColor.YELLOW)
              : NMLang.GUI_STATUS_RUNNING.translateColored(EnumColor.BRIGHT_GREEN)));
        double temperature = data.temperatureK;
        return List.of(
              NMLang.GUI_STATUS.translate().m_7220_(status),
              NMLang.GUI_BURN_RATE.translate().m_7220_(
                    NMLang.GUI_BURN_RATE_VALUE.translateColored(EnumColor.BRIGHT_GREEN, format(data.lastBurnRate, 2))),
              NMLang.GUI_FLUX.translate().m_7220_(
                    NMLang.GUI_FLUX_VALUE.translateColored(EnumColor.BRIGHT_GREEN, format(data.lastBurnRate / MB_PER_ITEM, 3))),
              NMLang.GUI_TEMPERATURE.translate().m_7220_(
                    NMLang.GUI_TEMP_VALUE.translateColored(colorOf(temperature), format(temperature, 1))),
              NMLang.GUI_STABLE.translate().m_7220_(
                    NMLang.GUI_STABLE_VALUE.translateColored(EnumColor.BRIGHT_GREEN, data.stableTicks,
                          data.activeRecipeStableTicks)));
    }

    /** 曲线标题行右侧：显示"当前配方"的温度窗口 + 产物（+ 进度）。没配方时显示"无配方"。 */
    private Component recipeLine() {
        PhaseChangeMultiblockData data = data();
        if (data.activeRecipeMaxTemp <= data.activeRecipeMinTemp) {
            return NMLang.GUI_RECIPE_NONE.translateColored(EnumColor.DARK_GRAY);
        }
        Component product = data.activeRecipeProduct.m_41619_()
              ? NMLang.GUI_PRODUCT_NONE.translate()
              : data.activeRecipeProduct.m_41786_();
        MutableComponent line = NMLang.GUI_RECIPE_LINE.translateColored(EnumColor.DARK_GRAY,
              number(data.activeRecipeMinTemp), number(data.activeRecipeMaxTemp), product);
        if (data.activeRecipeInWindow) {
            line.m_7220_(NMLang.GUI_RECIPE_PROGRESS.translateColored(EnumColor.DARK_GRAY,
                  Math.round(data.recipeProgress * 100.0D)));
        }
        return line;
    }

    /** 温度显示：整数就不带小数点（373.15 / 773）。 */
    private static String number(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.005D) {
            return String.valueOf((long) Math.rint(value));
        }
        return String.format("%.2f", value);
    }

    private Component coolantLabel() {
        PhaseChangeMultiblockData data = data();
        if (!data.fluidCoolantTank.isEmpty()) {
            return NMLang.GUI_COOLANT_WATER.translate();
        }
        if (!data.gasCoolantTank.isEmpty()) {
            return NMLang.GUI_COOLANT_SODIUM.translate();
        }
        return NMLang.GUI_COOLANT_NONE.translate();
    }

    private Component productLabel() {
        PhaseChangeMultiblockData data = data();
        if (!data.fluidCoolantTank.isEmpty()) {
            return NMLang.GUI_PRODUCT_STEAM.translate();
        }
        if (!data.gasCoolantTank.isEmpty()) {
            return NMLang.GUI_PRODUCT_SODIUM.translate();
        }
        return NMLang.GUI_PRODUCT_NONE.translate();
    }

    private int burnRatePercent() {
        return (int) Math.round(data().burnRateLimit * 100.0D);
    }

    private void setBurnRatePercent(int percent) {
        NMNetwork.setBurnRate(tile.m_58899_(), percent / 100.0D);
    }

    private void drawGaugeLabel(GuiGraphics guiGraphics, int gaugeX, Component label) {
        // 仪表宽 18，标签居中
        drawString(guiGraphics, label, gaugeX + (18 - getStringWidth(label)) / 2, 74, INK);
    }

    private void drawArrow(GuiGraphics guiGraphics, int x, int y) {
        int color = 0xFF404040;
        guiGraphics.m_280509_(x, y + 5, x + 12, y + 6, color);
        guiGraphics.m_280509_(x + 8, y + 2, x + 9, y + 5, color);
        guiGraphics.m_280509_(x + 9, y + 3, x + 10, y + 5, color);
        guiGraphics.m_280509_(x + 8, y + 6, x + 9, y + 9, color);
        guiGraphics.m_280509_(x + 9, y + 6, x + 10, y + 8, color);
    }

    private static EnumColor colorOf(double temperature) {
        if (temperature >= 1200.0D) {
            return EnumColor.ORANGE;
        }
        if (temperature >= 773.0D) {
            return EnumColor.YELLOW;
        }
        if (temperature >= 373.15D) {
            return EnumColor.BRIGHT_GREEN;
        }
        return EnumColor.AQUA;
    }

    private static String format(double value, int digits) {
        return String.format("%." + digits + "f", value);
    }
}
