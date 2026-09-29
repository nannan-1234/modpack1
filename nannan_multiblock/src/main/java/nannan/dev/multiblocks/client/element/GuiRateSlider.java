package nannan.dev.multiblocks.client.element;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import nannan.dev.multiblocks.registry.NMLang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * 燃烧速率滑块（设计稿里主页面 x=68, y=85, 121 × 22）。
 *
 * <p>**为什么自己画**：Mekanism 没有滑块控件——裂变反应堆的"设置速率上限"是文本框 + ✓。
 * 这里按 Mekanism 的观感画一条轨道 + 手柄，拖动时把 0..100 的整数交给回调（回调负责发包）。</p>
 *
 * <p>元素内部布局：y=0..8 是标签行，y=10..22 是轨道（12 高，与温度条等高）。</p>
 */
public class GuiRateSlider extends GuiElement {

    private static final int LABEL_HEIGHT = 8;
    private static final int TRACK_Y = 10;
    private static final int TRACK_HEIGHT = 12;
    private static final int TRACK_BORDER = 0xFF7C7C7C;
    private static final int TRACK_BG = 0xFF2B2B2B;
    private static final int FILL_COLOR = 0xFF55FF55;
    private static final int HANDLE_COLOR = 0xFFE8E8E8;
    private static final int TEXT_COLOR = 0x404040;
    private static final int LABEL_LINE = 0;

    private final IntSupplier percentSupplier;
    private final IntConsumer percentSetter;
    private boolean dragging;

    public GuiRateSlider(IGuiWrapper gui, int x, int y, int width, int height,
          IntSupplier percentSupplier, IntConsumer percentSetter) {
        super(gui, x, y, width, height);
        this.percentSupplier = percentSupplier;
        this.percentSetter = percentSetter;
    }

    @Override
    public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int left = relativeX;
        int top = relativeY + TRACK_Y;
        int right = left + f_93618_;
        int bottom = top + TRACK_HEIGHT;
        guiGraphics.m_280509_(left, top, right, bottom, TRACK_BG);
        // 轨道外框（上/左暗、下/右亮，和原版凹槽一致）
        guiGraphics.m_280509_(left, top, right, top + 1, 0xFF373737);
        guiGraphics.m_280509_(left, top, left + 1, bottom, 0xFF373737);
        guiGraphics.m_280509_(left, bottom - 1, right, bottom, TRACK_BORDER);
        guiGraphics.m_280509_(right - 1, top, right, bottom, TRACK_BORDER);
        int percent = clamp(percentSupplier.getAsInt());
        int filled = 1 + (int) Math.round((f_93618_ - 2) * (percent / 100.0D));
        guiGraphics.m_280509_(left + 1, top + 1, left + filled, bottom - 1, FILL_COLOR);
        // 手柄
        int handleX = Math.min(left + filled, right - 5);
        guiGraphics.m_280509_(handleX, top, handleX + 4, bottom, HANDLE_COLOR);
        guiGraphics.m_280509_(handleX, top, handleX + 1, bottom, 0xFFFFFFFF);
        guiGraphics.m_280509_(handleX + 3, top, handleX + 4, bottom, 0xFF555555);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component label = NMLang.GUI_RATE_SLIDER.translate()
              .m_7220_(NMLang.GUI_RATE_PERCENT.translateColored(EnumColor.BRIGHT_GREEN, clamp(percentSupplier.getAsInt())));
        drawString(guiGraphics, label, relativeX, relativeY + LABEL_LINE, TEXT_COLOR);
    }

    @Override
    public GuiElement mouseClickedNested(double mouseX, double mouseY, int button) {
        if (button == 0 && m_5953_(mouseX, mouseY)) {
            dragging = true;
            updateFromMouse(mouseX);
            return this;
        }
        dragging = false;
        return super.mouseClickedNested(mouseX, mouseY, button);
    }

    @Override
    public void m_7212_(double mouseX, double mouseY, double dragX, double dragY) {
        if (dragging) {
            updateFromMouse(mouseX);
            return;
        }
        super.m_7212_(mouseX, mouseY, dragX, dragY);
    }

    @Override
    public void m_7691_(double mouseX, double mouseY) {
        dragging = false;
        super.m_7691_(mouseX, mouseY);
    }

    private void updateFromMouse(double mouseX) {
        // mouseX 是屏幕绝对坐标，先换算到轨道内部（1..width-1）。
        double local = (mouseX - getGuiLeft() - relativeX - 1) / (f_93618_ - 2.0D);
        int percent = (int) Math.round(Math.max(0.0D, Math.min(1.0D, local)) * 100.0D);
        if (percent != clamp(percentSupplier.getAsInt())) {
            // 只在整数值真的变化时发包，避免拖动时刷屏。
            percentSetter.accept(percent);
        }
    }

    private static int clamp(int percent) {
        return Math.max(0, Math.min(100, percent));
    }
}
