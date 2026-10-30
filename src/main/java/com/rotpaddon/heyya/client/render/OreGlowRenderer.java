package com.rotpaddon.heyya.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.rotpaddon.heyya.entity.OreGlowEntity;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

/** Рисует блок руды на месте самой руды (чуть больше, чтобы не мерцали совпадающие грани) на полной яркости. */
public class OreGlowRenderer extends EntityRenderer<OreGlowEntity> {
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final float SCALE = 1.01F;

    public OreGlowRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(OreGlowEntity entity, float yRotation, float partialTick, MatrixStack matrixStack,
            IRenderTypeBuffer buffer, int packedLight) {
        BlockState state = entity.getOreState();
        if (state.isAir()) {
            return;     // данные о блоке ещё не пришли с сервера
        }
        matrixStack.pushPose();
        // Сущность стоит в центре низа блока: переносим центр куба в начало координат, увеличиваем, возвращаем
        matrixStack.translate(0.0D, 0.5D, 0.0D);
        matrixStack.scale(SCALE, SCALE, SCALE);
        matrixStack.translate(-0.5D, -0.5D, -0.5D);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                state, matrixStack, buffer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        matrixStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(OreGlowEntity entity) {
        return AtlasTexture.LOCATION_BLOCKS;
    }
}
