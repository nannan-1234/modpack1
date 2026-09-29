package nannan.dev.multiblocks.client.element;

import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import nannan.dev.multiblocks.network.NMNetwork;
import nannan.dev.multiblocks.phasechange.PhaseChangePartTile;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * 窗口左侧的"主页 / 统计"标签（26 × 26，挂在窗口外，坐标是负的）。
 *
 * <p>Mekanism 的标签贴图在 {@code mekanism:gui/tabs/} 里，但每个标签都要配一张专门的图；
 * 这里直接按设计稿画一个双色斜面的小方块，省掉贴图资源，外观与裂变反应堆左侧标签一致。</p>
 *
 * <p>点击后**不直接换屏幕**，而是发一个包让服务端打开另一个容器：页面切换在 Mekanism 里就是
 * "换容器"，两边容器不同步会导致槽位/同步数据错位。</p>
 */
public class GuiPageTab extends GuiElement {

    private static final int SIZE = 26;
    private static final int TEXT_COLOR = 0x404040;

    private final PhaseChangePartTile tile;
    private final int page;
    private final Component label;

    public GuiPageTab(IGuiWrapper gui, PhaseChangePartTile tile, int page, ILangEntry label, int x, int y) {
        super(gui, x, y, SIZE, SIZE);
        this.tile = tile;
        this.page = page;
        this.label = label.translate();
    }

    @Override
    public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // 坐标是"GUI 相对坐标"：元素背景这一遍的 pose 已经平移到窗口左上角，
        // 所以画图要用 relativeX / relativeY（Mekanism 自己的元素也是这么写的）。
        int x = relativeX;
        int y = relativeY;
        guiGraphics.m_280509_(x, y, x + SIZE, y + SIZE, m_5953_(mouseX, mouseY) ? 0xFFD8D8D8 : 0xFFC6C6C6);
        guiGraphics.m_280509_(x, y, x + SIZE, y + 1, 0xFFFFFFFF);
        guiGraphics.m_280509_(x, y, x + 1, y + SIZE, 0xFFFFFFFF);
        guiGraphics.m_280509_(x, y + SIZE - 1, x + SIZE, y + SIZE, 0xFF555555);
        guiGraphics.m_280509_(x + SIZE - 1, y, x + SIZE, y + SIZE, 0xFF555555);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        drawString(guiGraphics, label, relativeX + (SIZE - getStringWidth(label)) / 2, relativeY + 9, TEXT_COLOR);
    }

    @Override
    public GuiElement mouseClickedNested(double mouseX, double mouseY, int button) {
        if (button == 0 && m_5953_(mouseX, mouseY)) {
            // BlockEntity.getBlockPos() 的 SRG 名是 m_58899_。
            NMNetwork.openPage(tile.m_58899_(), page);
            return this;
        }
        return super.mouseClickedNested(mouseX, mouseY, button);
    }
}
