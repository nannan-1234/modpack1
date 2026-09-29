package nannan.dev.multiblocks.client.jei;

import java.text.DecimalFormat;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import nannan.dev.multiblocks.phasechange.recipe.PhaseChangeRecipe;
import nannan.dev.multiblocks.registry.NMBlocks;
import nannan.dev.multiblocks.registry.NMLang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * JEI 类别：一条相变配方 = "输入物品 → 产物 + 目标温度区间"。
 *
 * <p>用户明确要求"JEI 里要显示需要的材料和温度"，所以除了两个槽位，下面三行分别写
 * **温度区间 / 反应通量 / 稳定 tick**。背景用空白底图，箭头用 JEI 自带的，文本直接画。</p>
 */
public class PhaseChangeRecipeCategory implements IRecipeCategory<PhaseChangeRecipe> {

    private static final int WIDTH = 150;
    private static final int HEIGHT = 74;
    private static final int TEXT_COLOR = 0x404040;
    private static final DecimalFormat DEGREES = new DecimalFormat("#.##");

    private final IDrawable icon;
    private final IDrawable arrow;

    public PhaseChangeRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(NMBlocks.CONTROL_FURNACE_CONTROLLER.getBlock()));
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<PhaseChangeRecipe> getRecipeType() {
        return NMJeiPlugin.CONVERSION;
    }

    @Override
    public Component getTitle() {
        return NMLang.GUI_JEI_CATEGORY.translate();
    }

    // 不用 getBackground()（JEI 15.48 已标记为将删除）：只给尺寸，边框交给 JEI 的默认 recipe border。
    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PhaseChangeRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(10, 4).addIngredients(recipe.getInput());
        if (recipe.hasOutput()) {
            builder.addOutputSlot(62, 4).addItemStack(recipe.getOutput()).setOutputSlotBackground();
        }
    }

    @Override
    public void draw(PhaseChangeRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics guiGraphics, double mouseX,
          double mouseY) {
        arrow.draw(guiGraphics, 36, 6);
        // Minecraft.font 是原版公开字段，SRG 名是 f_91062_。
        Font font = Minecraft.m_91087_().f_91062_;
        int y = 30;
        drawLine(guiGraphics, font, 10, y, NMLang.GUI_JEI_TEMPERATURE.translate(
              DEGREES.format(recipe.getMinTemperature()), DEGREES.format(recipe.getMaxTemperature())));
        drawLine(guiGraphics, font, 10, y + 12, NMLang.GUI_JEI_FLUX.translate(recipe.getMbPerItem()));
        drawLine(guiGraphics, font, 10, y + 24, NMLang.GUI_JEI_STABLE.translate(recipe.getStableTicks()));
    }

    private static void drawLine(GuiGraphics guiGraphics, Font font, int x, int y, Component text) {
        // GuiGraphics.drawString(Font, Component, x, y, color, dropShadow) 的 SRG 名是 m_280614_。
        guiGraphics.m_280614_(font, text, x, y, TEXT_COLOR, false);
    }
}
