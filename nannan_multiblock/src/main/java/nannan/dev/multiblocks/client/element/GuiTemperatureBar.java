package nannan.dev.multiblocks.client.element;

import java.util.function.DoubleSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import nannan.dev.multiblocks.client.RecipeWindow;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * 温度条（设计稿里 x=30, y=111, 159 × 12）。
 *
 * <p>刻度只有两条**物理**线：沸点 373.15 K（冷却剂从这条线开始带走热量）与停堆温度 1500 K
 * （{@code MAX_TEMPERATURE}）。设计稿里的 773 K 是**示例配方的分界**，等配方系统接入后
 * 应该由数据生成"配方窗口色带"，而不是画在这里的固定刻度（决策 16）。</p>
 */
public class GuiTemperatureBar extends GuiElement {

    private static final double BOIL_TEMPERATURE = 373.15D;
    private static final double MAX_TEMPERATURE = 1500.0D;
    private static final int TRACK_BORDER = 0xFF7C7C7C;
    private static final int TRACK_BG = 0xFF2B2B2B;
    private static final int TICK_COLOR = 0xFFFFFFFF;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int TEXT_SHADOW = 0xFF000000;

    private final DoubleSupplier temperature;
    private final RecipeWindow window;

    public GuiTemperatureBar(IGuiWrapper gui, int x, int y, int width, int height, DoubleSupplier temperature,
          RecipeWindow window) {
        super(gui, x, y, width, height);
        this.temperature = temperature;
        this.window = window;
    }

    @Override
    public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int left = relativeX;
        int top = relativeY;
        int right = left + f_93618_;
        int bottom = top + f_93619_;
        guiGraphics.m_280509_(left, top, right, bottom, TRACK_BG);
        guiGraphics.m_280509_(left, top, right, top + 1, 0xFF373737);
        guiGraphics.m_280509_(left, top, left + 1, bottom, 0xFF373737);
        guiGraphics.m_280509_(left, bottom - 1, right, bottom, TRACK_BORDER);
        guiGraphics.m_280509_(right - 1, top, right, bottom, TRACK_BORDER);

        double value = Math.max(0.0D, Math.min(MAX_TEMPERATURE, temperature.getAsDouble()));
        int filled = 1 + (int) Math.round((f_93618_ - 2) * (value / MAX_TEMPERATURE));
        guiGraphics.m_280509_(left + 1, top + 1, left + filled, bottom - 1, colorFor(value));

        // 当前配方的温度窗口：贴着条子顶部画一条小色带（亮 = 温度已在窗口里，暗 = 还在等温度）
        if (window != null && window.hasWindow()) {
            int windowLeft = left + 1 + (int) Math.round((f_93618_ - 2) * (window.min / MAX_TEMPERATURE));
            int windowRight = left + 1 + (int) Math.round((f_93618_ - 2) * (window.max / MAX_TEMPERATURE));
            guiGraphics.m_280509_(windowLeft, top + 1, Math.max(windowLeft + 1, windowRight), top + 4,
                  window.inWindow ? 0xFF55FFFF : 0x80808080);
        }

        drawTick(guiGraphics, left, top, bottom, BOIL_TEMPERATURE, "373");
        drawTick(guiGraphics, left, top, bottom, MAX_TEMPERATURE, "1500");
    }

    private void drawTick(GuiGraphics guiGraphics, int left, int top, int bottom, double tickTemperature, String label) {
        int x = left + (int) Math.round((f_93618_ - 2) * (tickTemperature / MAX_TEMPERATURE));
        guiGraphics.m_280509_(x, top, x + 1, bottom, TICK_COLOR);
        Component text = Component.m_237113_(label);
        int textX = label.length() > 3 ? x - getStringWidth(text) : x + 1;
        drawString(guiGraphics, text, textX, top + 2, TEXT_COLOR);
    }

    /** 温度配色（与设计稿一致）：青 → 绿 → 黄 → 橙。 */
    public static int colorFor(double temperature) {
        if (temperature >= 1200.0D) {
            return 0xFFFF9A3C;
        }
        if (temperature >= 773.0D) {
            return 0xFFFFD34D;
        }
        if (temperature >= BOIL_TEMPERATURE) {
            return 0xFF55FF55;
        }
        return 0xFF55FFFF;
    }
}
