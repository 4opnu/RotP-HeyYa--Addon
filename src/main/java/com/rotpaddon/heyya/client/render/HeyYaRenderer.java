package com.rotpaddon.heyya.client.render;

import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandModelRegistry;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.rotpaddon.heyya.AddonMain;
import com.rotpaddon.heyya.entity.HeyYaEntity;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class HeyYaRenderer extends StandEntityRenderer<HeyYaEntity, StandEntityModel<HeyYaEntity>> {

    public HeyYaRenderer(EntityRendererManager renderManager) {
        super(renderManager,
                StandModelRegistry.registerModel(new ResourceLocation(AddonMain.MOD_ID, "hey_ya"), HeyYaModel::new),
                new ResourceLocation(AddonMain.MOD_ID, "textures/entity/stand/hey_ya.png"), 0);
    }

    @Override
    public void render(HeyYaEntity entity, float yRotation, float partialTick, MatrixStack matrixStack,
            IRenderTypeBuffer buffer, int packedLight) {
        matrixStack.pushPose();

        // Приклеиваем стенд к игроку на КАЖДОМ кадре: позиция берётся из плавной позиции владельца,
        // а не из позиции самой сущности, которая может отставать на тик.
        LivingEntity user = entity.getUser();
        if (user != null) {
            Vector3d target = HeyYaEntity.getHangPosition(user, partialTick, entity.getSkin(), entity.tickCount + partialTick);
            double dx = target.x - MathHelper.lerp((double) partialTick, entity.xo, entity.getX());
            double dy = target.y - MathHelper.lerp((double) partialTick, entity.yo, entity.getY());
            double dz = target.z - MathHelper.lerp((double) partialTick, entity.zo, entity.getZ());
            matrixStack.translate(dx, dy, dz);
            yRotation = MathHelper.rotLerp(partialTick, user.yBodyRotO, user.yBodyRot);
        }

        float scale = entity.getSkin() == HeyYaEntity.SKIN_CHARA ? HeyYaEntity.CHARA_MODEL_SCALE : HeyYaEntity.MODEL_SCALE;
        matrixStack.scale(scale, scale, scale);
        // Текстуру и модель выбранного скина подставляет сам RotP (меню JoJo -> Stand Skins)
        super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
        matrixStack.popPose();
    }
}
