package com.reiryoku_expansion.client;

import com.reiryoku_expansion.ae2.ReiryokuKey;

import appeng.api.client.AEKeyRenderHandler;
import appeng.client.gui.style.Blitter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

/**
 * Client render handler for Reiryoku keys. Without this AE2 throws
 * {@code Missing render handler for channel reiryoku_expansion:reiryoku} when the
 * terminal tries to draw or describe a Reiryoku entry.
 *
 * <p>The GUI icon is Urushi's element magatama texture; the block-face icon is
 * a flat quad in the element's color (mirrors Applied Botanics' ManaRenderer
 * structure, but without requiring a block-atlas sprite).</p>
 */
public class ReiryokuKeyRenderer implements AEKeyRenderHandler<ReiryokuKey> {

    @Override
    public void drawInGui(Minecraft minecraft, GuiGraphics guiGraphics, int x, int y, ReiryokuKey key) {
        Blitter.texture(new ResourceLocation("urushi",
                        "textures/item/magatama_" + key.getElement().getLangKey() + ".png"), 16, 16)
                .dest(x, y, 16, 16)
                .blit(guiGraphics);
    }

    @Override
    public void drawOnBlockFace(PoseStack poseStack, MultiBufferSource buffers, ReiryokuKey key, float width,
                                int light, Level level) {
        poseStack.pushPose(); // pushPose
        poseStack.translate(0.0F, 0.0F, 0.01F); // translate
        VertexConsumer buffer = buffers.getBuffer(RenderType.solid()); // getBuffer(gui)
        float half = (width - 0.05F) / 2.0F;
        Matrix4f mat = poseStack.last().pose(); // last().pose()
        int color = key.getElement().getArgbColor();
        vertex(buffer, mat, -half, -half, 0.0F, color, light);
        vertex(buffer, mat, half, -half, 0.0F, color, light);
        vertex(buffer, mat, half, half, 0.0F, color, light);
        vertex(buffer, mat, -half, half, 0.0F, color, light);
        poseStack.popPose(); // popPose
    }

    private static void vertex(VertexConsumer buffer, Matrix4f mat, float x, float y, float z, int color, int light) {
        buffer.vertex(mat, x, y, z) // vertex
                .color(color) // color
                .uv(0.0F, 0.0F) // uv
                .overlayCoords(OverlayTexture.NO_OVERLAY) // overlayCoords(NO_OVERLAY)
                .uv2(light) // uv2
                .normal(0.0F, 0.0F, 1.0F) // normal
                .endVertex(); // endVertex
    }

    @Override
    public Component getDisplayName(ReiryokuKey key) {
        return key.getDisplayName();
    }
}
