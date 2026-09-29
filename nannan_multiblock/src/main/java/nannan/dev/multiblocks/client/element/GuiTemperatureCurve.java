package nannan.dev.multiblocks.client.element;

import java.util.function.DoubleSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import nannan.dev.multiblocks.client.RecipeWindow;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * 温度曲线（设计稿里 x=6, y=137, 183 × 40），取代裂变反应堆的"产热图形"。
 *
 * <p>数据是**客户端自己采样**的：每个客户端 tick 由屏幕把当前温度 {@link #addSample()} 进来，
 * 不需要同步一整个数组（裂变反应堆的 {@code GuiDoubleGraph} 也是这么做的）。
 * 时间常数 {@code τ = 1 / k} 只有 2 tick 左右，所以曲线看上去是"阶梯"而不是缓慢爬升——
 * 这正是它有用的地方：看的是稳态值与跳变，而不是升温过程。</p>
 */
public class GuiTemperatureCurve extends GuiElement {

    /** 采样点数量：100 点 ≈ 5 秒（20 tps）。 */
    private static final int SAMPLES = 100;
    private static final double BOIL_TEMPERATURE = 373.15D;
    private static final double MAX_TEMPERATURE = 1500.0D;
    private static final double AXIS_MIN = 300.0D;
    private static final double AXIS_MAX = 1600.0D;
    private static final int BACKGROUND = 0xFF000000;
    private static final int FRAME = 0xFF6F6F6F;
    private static final int CURVE_COLOR = 0xFF55FF55;
    private static final int BOIL_COLOR = 0xFF55FFFF;
    private static final int MAX_COLOR = 0xFFFF5555;

    private final DoubleSupplier temperature;
    private final RecipeWindow window;
    private final double[] history = new double[SAMPLES];

    public GuiTemperatureCurve(IGuiWrapper gui, int x, int y, int width, int height, DoubleSupplier temperature,
          RecipeWindow window) {
        super(gui, x, y, width, height);
        this.temperature = temperature;
        this.window = window;
        for (int i = 0; i < SAMPLES; i++) {
            history[i] = 300.0D;
        }
    }

    /** 屏幕在 {@code containerTick} 里调用：把当前温度接到曲线尾部（内部自己左移一格）。 */
    public void addSample() {
        System.arraycopy(history, 1, history, 0, SAMPLES - 1);
        history[SAMPLES - 1] = temperature.getAsDouble();
    }

    @Override
    public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int left = relativeX;
        int top = relativeY;
        int right = left + f_93618_;
        int bottom = top + f_93619_;
        guiGraphics.m_280509_(left, top, right, bottom, BACKGROUND);
        guiGraphics.m_280509_(left, top, right, top + 1, FRAME);
        guiGraphics.m_280509_(left, bottom - 1, right, bottom, FRAME);
        guiGraphics.m_280509_(left, top, left + 1, bottom, FRAME);
        guiGraphics.m_280509_(right - 1, top, right, bottom, FRAME);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int left = relativeX + 1;
        int right = relativeX + f_93618_ - 1;
        int top = relativeY + 1;
        int bottom = relativeY + f_93619_ - 1;
        int plotLeft = left + 18;   // 左侧留给温度刻度文字

        // 先画"目标温度窗口"的横向色带，再画参考线与曲线，这样曲线压在色带上面
        if (window != null && window.hasWindow()) {
            int bandTop = temperatureY(window.max, top, bottom);
            int bandBottom = temperatureY(window.min, top, bottom);
            guiGraphics.m_280509_(plotLeft, bandTop, right - 1, Math.max(bandTop + 1, bandBottom),
                  window.inWindow ? 0x4C55FFFF : 0x33808080);
        }

        dashedLine(guiGraphics, plotLeft, right, temperatureY(BOIL_TEMPERATURE, top, bottom), BOIL_COLOR);
        dashedLine(guiGraphics, plotLeft, right, temperatureY(MAX_TEMPERATURE, top, bottom), MAX_COLOR);
        drawString(guiGraphics, Component.m_237113_("373"), left + 1, temperatureY(BOIL_TEMPERATURE, top, bottom) - 4, BOIL_COLOR);
        drawString(guiGraphics, Component.m_237113_("1500"), left + 1, temperatureY(MAX_TEMPERATURE, top, bottom) - 1, MAX_COLOR);

        int span = Math.max(1, right - plotLeft - 1);
        int previousY = temperatureY(history[0], top, bottom);
        for (int i = 1; i < SAMPLES; i++) {
            int x = plotLeft + (int) Math.round((double) i / (SAMPLES - 1) * span);
            int y = temperatureY(history[i], top, bottom);
            // 竖线连上一点（像素风折线），再点一个像素
            guiGraphics.m_280509_(x, Math.min(previousY, y), x + 1, Math.max(previousY, y) + 1, CURVE_COLOR);
            previousY = y;
        }
    }

    private int temperatureY(double value, int top, int bottom) {
        double clamped = Math.max(AXIS_MIN, Math.min(AXIS_MAX, value));
        double ratio = (clamped - AXIS_MIN) / (AXIS_MAX - AXIS_MIN);
        return bottom - 1 - (int) Math.round(ratio * (bottom - top - 2));
    }

    private static void dashedLine(GuiGraphics guiGraphics, int left, int right, int y, int color) {
        for (int x = left; x < right; x += 6) {
            guiGraphics.m_280509_(x, y, Math.min(x + 3, right), y + 1, color);
        }
    }
}
