package com.rotpaddon.heyya.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.rotpaddon.heyya.entity.HeyYaPickupEntity;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

/**
 * Рисует блок булыжника, наполовину погружённый в землю. Поворот и небольшой наклон зависят от UUID,
 * поэтому камни лежат по-разному. Размер задаётся SCALE (1.0 - целый блок, 0.55 - примерно полблока).
 */
public class HeyYaPickupRenderer extends EntityRenderer<HeyYaPickupEntity> {
    private static final float SCALE = 0.55F;
    /** Минимальная яркость камня (0..15): он светится слабее фонаря, но заметно ярче окружения. */
    private static final int MIN_BLOCK_LIGHT = 12;

    public HeyYaPickupRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(HeyYaPickupEntity entity, float yRotation, float partialTick, MatrixStack matrixStack,
            IRenderTypeBuffer buffer, int packedLight) {
        int hash = entity.getUUID().hashCode();
        float yaw = (hash & 255) * (360.0F / 256.0F);
        float tiltX = ((hash >> 8) & 15) - 7.5F;
        float tiltZ = ((hash >> 12) & 15) - 7.5F;

        // Подсветка: поднимаем блочный свет до MIN_BLOCK_LIGHT, небесный свет оставляем как есть
        int blockLight = Math.max((packedLight >> 4) & 15, MIN_BLOCK_LIGHT);
        int skyLight = (packedLight >> 20) & 15;
        int litLight = (skyLight << 20) | (blockLight << 4);

        matrixStack.pushPose();
        // Сущность стоит на поверхности земли: центр куба в начале координат = ровно половина блока в земле
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(yaw));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(tiltX));
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(tiltZ));
        matrixStack.scale(SCALE, SCALE, SCALE);
        matrixStack.translate(-0.5D, -0.5D, -0.5D);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                Blocks.COBBLESTONE.defaultBlockState(), matrixStack, buffer, litLight, OverlayTexture.NO_OVERLAY);
        matrixStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(HeyYaPickupEntity entity) {
        return AtlasTexture.LOCATION_BLOCKS;
    }
}
