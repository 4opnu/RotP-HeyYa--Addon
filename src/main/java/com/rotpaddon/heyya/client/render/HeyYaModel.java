package com.rotpaddon.heyya.client.render;

import com.github.standobyte.jojo.client.render.entity.model.stand.HumanoidStandModel;
import com.github.standobyte.jojo.client.render.entity.pose.IModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.RotationAngle;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.rotpaddon.heyya.entity.HeyYaEntity;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Модель Hey Ya! (из Blockbench пользователя).
 * Кости jaw и skull лежат в geo-файле (assets/heyya/geo/hey_ya.geo.json),
 * поля с теми же именами заполняются загрузчиком модели RotP.
 *
 * Анимации:
 *  1) "Висит на спине": руки подняты и обхватывают плечи игрока, тело раскачивается как маятник
 *     вокруг точки подвеса (шея), ноги болтаются и отлетают назад, когда игрок бежит.
 *  2) "Разговор": челюсть (jaw) открывается и закрывается, череп (skull) покачивается.
 */
public class HeyYaModel extends HumanoidStandModel<HeyYaEntity> {
    private ModelRenderer jaw;
    private ModelRenderer skull;

    // Значения для обычного стенда. Наклон черепа из исходной модели Blockbench: 2.5 градуса, перед чуть вверх
    private static final float DEFAULT_SKULL_TILT = -0.0436F;

    // Параметры текущего скина (обновляются в setupAnim): сужение по ширине и базовый наклон черепа
    private float widthScale = HeyYaEntity.MODEL_WIDTH_SCALE;
    private float skullTilt = DEFAULT_SKULL_TILT;
    // Руки подняты вперёд-вверх (около 115 градусов), ладони ложатся на плечо игрока
    private static final float HANG_ARM_X = -2.0F;

    public HeyYaModel() {
        super();
    }

    @Override // TODO summon poses
    protected RotationAngle[][] initSummonPoseRotations() {
        return new RotationAngle[][] {
            new RotationAngle[] {

            },
            new RotationAngle[] {

            }
        };
    }

    @Override
    protected ModelPose<HeyYaEntity> initIdlePose() {
        return super.initIdlePose();
    }

    @Override
    protected IModelPose<HeyYaEntity> initIdlePose2Loop() {
        return super.initIdlePose2Loop();
    }

    // Сужаем модель по ширине (локальная ось X модели): рост, глубина и текстура не меняются.
    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay,
            float red, float green, float blue, float alpha) {
        matrixStack.pushPose();
        matrixStack.scale(widthScale, 1.0F, 1.0F);
        super.renderToBuffer(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        matrixStack.popPose();
    }

    @Override
    public void setupAnim(HeyYaEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks,
            float yRotationOffset, float xRotation) {
        // Скин "Цветок": без сужения по ширине и без наклона черепа
        boolean flowey = entity.getSkin() == HeyYaEntity.SKIN_FLOWEY;
        boolean chara = entity.getSkin() == HeyYaEntity.SKIN_CHARA;
        widthScale = (flowey || chara) ? 1.0F : HeyYaEntity.MODEL_WIDTH_SCALE;
        skullTilt = (flowey || chara) ? 0.0F : DEFAULT_SKULL_TILT;

        super.setupAnim(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation);
        if (chara) {
            applyCharaAnimation(ticks);
        }
        else {
            applyHangingPose(entity, ticks);
        }
        applyTalkAnimation(entity, ticks);
    }

    /** Скорость владельца в блоках/тик по горизонтали - от неё зависит раскачка. */
    private static float userSpeed(HeyYaEntity entity) {
        LivingEntity user = entity.getUser();
        if (user == null) {
            return 0;
        }
        Vector3d motion = user.getDeltaMovement();
        return (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);
    }

    // Положения костей до анимации (загрузчик задаёт их один раз): к ним добавляются смещения из анимации
    private final Map<ModelRenderer, float[]> basePositions = new IdentityHashMap<>();

    /**
     * Скин Chara: анимации из Blockbench (CharaAnimations). При призыве один раз играет Summon,
     * сразу после неё Idle по кругу, пока стенд призван. Время считается от появления стенда.
     * Повороты в Blockbench (x, y, z) переводятся в повороты модели игры как (-x, -y, z),
     * а смещения по (x, y, z) как (-x, -y, z): оси модели в игре повёрнуты на 180 градусов вокруг оси Z.
     */
    private void applyCharaAnimation(float ticks) {
        float t = ticks / 20.0F;
        boolean summon = t < CharaAnimations.SUMMON_LENGTH;
        float time = summon ? t : (t - CharaAnimations.SUMMON_LENGTH) % CharaAnimations.IDLE_LOOP_LENGTH;
        CharaAnimations.Clip clip = summon ? CharaAnimations.SUMMON : CharaAnimations.IDLE;

        if (body != null) {
            body.xRot = 0.0F;
            body.yRot = 0.0F;
            body.zRot = 0.0F;
        }
        ModelRenderer[] parts = { upperPart, head, torso, rightArm, leftArm, rightLeg, leftLeg };
        for (int bone = 0; bone < parts.length; bone++) {
            applyAnimatedPart(parts[bone], bone, clip, time);
        }
    }

    private void applyAnimatedPart(ModelRenderer part, int bone, CharaAnimations.Clip clip, float time) {
        if (part == null) {
            return;
        }
        float[] rest = CharaAnimations.REST_ROTATION[bone];
        float[] rot = clip.rotation(bone, time);
        part.xRot = (float) Math.toRadians(-(rest[0] + rot[0]));
        part.yRot = (float) Math.toRadians(-(rest[1] + rot[1]));
        part.zRot = (float) Math.toRadians(rest[2] + rot[2]);

        float[] base = basePositions.get(part);
        if (base == null) {
            base = new float[] { part.x, part.y, part.z };
            basePositions.put(part, base);
        }
        float[] pos = clip.position(bone, time);
        if (bone == CharaAnimations.HEAD) {
            // Голова - корневая кость (RotP рисует её отдельно), поэтому смещение талии (Waist),
            // которое остальные части получают от родителя, добавляем ей вручную
            float[] waist = clip.position(CharaAnimations.WAIST, time);
            pos = new float[] { pos[0] + waist[0], pos[1] + waist[1], pos[2] + waist[2] };
        }
        part.x = base[0] - pos[0];
        part.y = base[1] - pos[1];
        part.z = base[2] + pos[2];
    }

    private void applyHangingPose(HeyYaEntity entity, float ticks) {
        float speed = userSpeed(entity);
        float run = Math.min(speed * 3.0F, 0.6F);          // 0..0.6: насколько игрок разогнался

        // Маятник: тело качается вокруг шеи (положительный угол - ноги назад)
        float pendulum = run * 0.8F + 0.03F * MathHelper.sin(ticks * 0.07F);
        if (body != null) {
            body.xRot = pendulum;
        }

        // Руки держатся за плечи: компенсируем наклон тела, чтобы ладони оставались на месте
        float breathe = 0.03F * MathHelper.sin(ticks * 0.1F);
        if (leftArm != null) {
            leftArm.xRot = HANG_ARM_X - pendulum + breathe;
            leftArm.yRot = 0;
            leftArm.zRot = 0;
        }
        if (rightArm != null) {
            rightArm.xRot = HANG_ARM_X - pendulum + breathe;
            rightArm.yRot = 0;
            rightArm.zRot = 0;
        }

        // Ноги болтаются; на бегу машут сильнее
        float legSwing = 0.10F + run * 0.5F;
        float legSpeed = 0.12F + run * 0.8F;
        if (leftLeg != null) {
            leftLeg.xRot = 0.10F + legSwing * MathHelper.sin(ticks * legSpeed);
        }
        if (rightLeg != null) {
            rightLeg.xRot = 0.10F + legSwing * MathHelper.sin(ticks * legSpeed + (float) Math.PI);
        }
    }

    private void applyTalkAnimation(HeyYaEntity entity, float ticks) {
        float partialTick = ticks - entity.tickCount;
        float talk = entity.getTalkIntensity(partialTick);

        // "Слоги": две накладывающиеся волны дают неровный, похожий на речь ритм (0..1)
        float syllable = 0.6F * Math.abs(MathHelper.sin(ticks * 0.85F))
                + 0.4F * Math.abs(MathHelper.sin(ticks * 1.6F + 1.3F));

        if (jaw != null) {
            // Положительный угол - передний край челюсти опускается (рот открывается)
            jaw.xRot = talk * (0.06F + 0.45F * syllable);
        }
        if (skull != null) {
            // На открытии рта череп слегка запрокидывается и покачивается из стороны в сторону
            skull.xRot = skullTilt - talk * 0.12F * syllable;
            skull.yRot = talk * 0.07F * MathHelper.sin(ticks * 0.45F);
            skull.zRot = talk * 0.04F * MathHelper.sin(ticks * 0.3F + 0.7F);
        }
    }
}
