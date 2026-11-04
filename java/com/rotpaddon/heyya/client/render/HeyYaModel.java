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
import net.minecraft.util.math.vector.Vector3f;

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
    // Кости только из geo скина Flowey (у остальных скинов null): стебель на спине (barHang, скрыт в танце),
    // стебель танца (chest1, виден только в танце) и верхняя часть стебля (chest2)
    private ModelRenderer barHang;
    // Сердце Chara (кость heart есть только в geo скина Chara, у остальных null): во время танца вылетает из груди
    private ModelRenderer heart;
    private ModelRenderer chest1;
    private ModelRenderer chest2;

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
        applyDanceRootTransform(matrixStack);
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
        captureBasePositions();
        danceRootActive = false;
        // Flowey в танце: стебель на спине прячется, вместо него виден стебель танца
        boolean floweyDance = flowey && entity.isDancing();
        if (barHang != null) {
            barHang.visible = !floweyDance;
        }
        if (chest1 != null) {
            chest1.visible = floweyDance;
        }
        if (chara) {
            positionsModified = true;
            if (entity.isDancing() && !entity.isUserSleeping()) {
                applyCharaDance(entity.getDanceCycleTime(ticks - entity.tickCount));
            }
            else {
                applyCharaAnimation(ticks);
            }
        }
        else if (entity.isUserSleeping()) {
            // Владелец спит в кровати: стенд лежит у него на животе. Обычный скин играет сон, остальные просто стоят
            if (entity.getSkin() == HeyYaEntity.SKIN_DEFAULT) {
                applySleep((ticks / 20.0F / SLEEP_SLOWDOWN) % SleepAnimation.LENGTH);
            }
            else {
                restoreBasePositions();
                applyStandingPose(ticks);
            }
        }
        else if (entity.isDancing()) {
            // Стенд стоит на блоке: обычный скин танцует по кругу (круг = длина музыки), остальные скины просто стоят
            if (entity.getSkin() == HeyYaEntity.SKIN_DEFAULT) {
                float cycleSeconds = entity.getDanceCycleTicks() / 20.0F;
                float cycleTime = entity.getDanceCycleTime(ticks - entity.tickCount);
                applyDance(cycleTime * DanceAnimation.LENGTH / cycleSeconds);
            }
            else if (flowey) {
                // Круг = длина музыки Flowey, за него анимация играется FLOWEY_DANCE_REPEATS раз (в такт музыке)
                float cycleSeconds = entity.getDanceCycleTicks() / 20.0F;
                float cycleTime = entity.getDanceCycleTime(ticks - entity.tickCount);
                float phase = (cycleTime / cycleSeconds * HeyYaEntity.FLOWEY_DANCE_REPEATS) % 1.0F;
                float time = phase * FloweyDanceAnimation.LENGTH;
                applyStandingPose(ticks);
                applyFloweyDance(time);
            }
            else {
                restoreBasePositions();
                applyStandingPose(ticks);
            }
        }
        else {
            restoreBasePositions();
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

    // ======================= Танец Chara (анимация из Dance_chara_4.bbmodel) =======================
    // Положение кости Waist (талия, upperPart) в Blockbench: вокруг этой точки она вращается; Head - дочерняя кость Waist
    private static final double[] CHARA_WAIST_PIVOT = {0, 12, 0};
    private static final double[] CHARA_HEAD_PIVOT = {0, 24, 0};

    /**
     * Играет танец Chara в момент time (секунды от начала круга, 0 - CharaDanceAnimation.CYCLE). Части двигаются так же,
     * как у Idle и Summon (applyAnimatedPart): базовый поворот кости + поворот кадра, углы (-x, -y, z), смещения (-x, -y, z).
     * Особенность: в Blockbench голова - дочерняя кость Waist, а в игре голова - корневая кость, поэтому её поворот и смещение
     * считаются сразу с учётом талии (в танце талия вращается, в Idle - нет).
     */
    private void applyCharaDance(float time) {
        if (body != null) {
            body.xRot = 0.0F;
            body.yRot = 0.0F;
            body.zRot = 0.0F;
        }
        float[][] rot = new float[7][];
        float[][] pos = new float[7][];
        for (int bone = 0; bone < 7; bone++) {
            float[] rest = CharaAnimations.REST_ROTATION[bone];
            float[] keyed = CharaDanceAnimation.rotation(bone, time);
            rot[bone] = new float[] { rest[0] + keyed[0], rest[1] + keyed[1], rest[2] + keyed[2] };
            pos[bone] = CharaDanceAnimation.position(bone, time);
        }
        placeCharaPart(upperPart, rot[CharaDanceAnimation.WAIST], pos[CharaDanceAnimation.WAIST]);
        placeCharaPart(torso, rot[CharaDanceAnimation.BODY], pos[CharaDanceAnimation.BODY]);
        placeCharaPart(rightArm, rot[CharaDanceAnimation.RIGHT_ARM], pos[CharaDanceAnimation.RIGHT_ARM]);
        placeCharaPart(leftArm, rot[CharaDanceAnimation.LEFT_ARM], pos[CharaDanceAnimation.LEFT_ARM]);
        placeCharaPart(rightLeg, rot[CharaDanceAnimation.RIGHT_LEG], pos[CharaDanceAnimation.RIGHT_LEG]);
        placeCharaPart(leftLeg, rot[CharaDanceAnimation.LEFT_LEG], pos[CharaDanceAnimation.LEFT_LEG]);
        placeCharaPart(heart, new float[3], CharaDanceAnimation.position(CharaDanceAnimation.HEART, time));

        // Голова = Waist * Head: R = Rw * Rh, T = Pw + Tw + Rw * (Q - Pw + Th) - Q (Q - точка вращения головы)
        double[][] rw = rotationMatrix(rot[CharaDanceAnimation.WAIST]);
        double[][] rh = rotationMatrix(rot[CharaDanceAnimation.HEAD]);
        double[] tw = toDouble(pos[CharaDanceAnimation.WAIST]);
        double[] th = toDouble(pos[CharaDanceAnimation.HEAD]);
        double[] inner = {
                CHARA_HEAD_PIVOT[0] - CHARA_WAIST_PIVOT[0] + th[0],
                CHARA_HEAD_PIVOT[1] - CHARA_WAIST_PIVOT[1] + th[1],
                CHARA_HEAD_PIVOT[2] - CHARA_WAIST_PIVOT[2] + th[2] };
        double[] rotated = multiply(rw, inner);
        float[] headPos = new float[3];
        for (int i = 0; i < 3; i++) {
            headPos[i] = (float) (CHARA_WAIST_PIVOT[i] + tw[i] + rotated[i] - CHARA_HEAD_PIVOT[i]);
        }
        placeCharaPart(head, eulerFromMatrix(multiply(rw, rh)), headPos);
    }

    /** Часть Chara: углы (-x, -y, z) и смещения (-x, -y, z) из Blockbench (как в applyAnimatedPart). */
    private void placeCharaPart(ModelRenderer part, float[] rot, float[] pos) {
        if (part == null) {
            return;
        }
        part.xRot = (float) Math.toRadians(-rot[0]);
        part.yRot = (float) Math.toRadians(-rot[1]);
        part.zRot = (float) Math.toRadians(rot[2]);
        float[] base = basePositions.get(part);
        if (base != null) {
            part.x = base[0] - pos[0];
            part.y = base[1] - pos[1];
            part.z = base[2] + pos[2];
        }
    }

    private static double[] toDouble(float[] v) {
        return new double[] { v[0], v[1], v[2] };
    }

    /** Матрица поворота Blockbench: сначала вокруг X, затем Y, затем Z (градусы): R = Rz * Ry * Rx. */
    private static double[][] rotationMatrix(float[] deg) {
        double rx = Math.toRadians(deg[0]), ry = Math.toRadians(deg[1]), rz = Math.toRadians(deg[2]);
        double cx = Math.cos(rx), sx = Math.sin(rx), cy = Math.cos(ry), sy = Math.sin(ry), cz = Math.cos(rz), sz = Math.sin(rz);
        return new double[][] {
                { cz * cy, cz * sy * sx - sz * cx, cz * sy * cx + sz * sx },
                { sz * cy, sz * sy * sx + cz * cx, sz * sy * cx - cz * sx },
                { -sy, cy * sx, cy * cx } };
    }

    private static double[][] multiply(double[][] a, double[][] b) {
        double[][] r = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                r[i][j] = a[i][0] * b[0][j] + a[i][1] * b[1][j] + a[i][2] * b[2][j];
            }
        }
        return r;
    }

    private static double[] multiply(double[][] m, double[] v) {
        return new double[] {
                m[0][0] * v[0] + m[0][1] * v[1] + m[0][2] * v[2],
                m[1][0] * v[0] + m[1][1] * v[1] + m[1][2] * v[2],
                m[2][0] * v[0] + m[2][1] * v[1] + m[2][2] * v[2] };
    }

    /** Углы (градусы) из матрицы R = Rz * Ry * Rx - обратное к rotationMatrix. */
    private static float[] eulerFromMatrix(double[][] m) {
        double sy = Math.max(-1.0, Math.min(1.0, -m[2][0]));
        double ry = Math.asin(sy);
        double rx;
        double rz;
        if (Math.abs(sy) < 0.999999) {
            rx = Math.atan2(m[2][1], m[2][2]);
            rz = Math.atan2(m[1][0], m[0][0]);
        }
        else {
            rz = 0.0;
            rx = Math.atan2(-m[1][2], m[1][1]);
        }
        return new float[] { (float) Math.toDegrees(rx), (float) Math.toDegrees(ry), (float) Math.toDegrees(rz) };
    }

    // ======================= Танец (анимация из Dance.bbmodel) =======================
    // Точка, вокруг которой в Dance.bbmodel вращаются все кости: (0, 26, 0)
    private static final float[] DANCE_PIVOT = {0F, 26F, 0F};
    // Точки вращения частей в игровой модели (из hey_ya.geo.json)
    private static final float[] PIVOT_BODY = {0F, 23F, 0F};
    private static final float[] PIVOT_HEAD = {0F, 23F, 0F};
    private static final float[] PIVOT_TORSO = {0F, 23F, 0F};
    private static final float[] PIVOT_LEFT_ARM = {5F, 20F, -1F};
    private static final float[] PIVOT_RIGHT_ARM = {-5F, 20F, -1F};
    private static final float[] PIVOT_LEFT_LEG = {3F, 10F, -1F};
    private static final float[] PIVOT_RIGHT_LEG = {-3F, 10F, -1F};

    /**
     * Зеркальный танец: в игре модель отражена по X относительно Blockbench, и без этого кувырки крутились в обратную сторону.
     * true - поворот по Y и Z и смещение по X меняют знак, а левые и правые кости меняются местами.
     */
    private static final boolean MIRROR_DANCE = true;
    /** false - голова из танца не двигается (поворачивается вместе с телом, без своих кадров). */
    private static final boolean DANCE_HEAD_ANIMATION = true;

    /** Анимация из Blockbench с общей схемой костей (танец и сон): кадры костей + смещение кости body (несёт торс и руки). */
    private interface BoneClip {
        float[] rotation(int bone, float time);
        float[] position(int bone, float time);
        float[] bodyOffset(float time);
        /** true - анимация голову двигает своими кадрами (для танца это можно отключить, DANCE_HEAD_ANIMATION). */
        boolean animatesHead();
    }

    private static final BoneClip DANCE_CLIP = new BoneClip() {
        public float[] rotation(int bone, float time) { return DanceAnimation.rotation(bone, time); }
        public float[] position(int bone, float time) { return DanceAnimation.position(bone, time); }
        public float[] bodyOffset(float time) { return DanceAnimation.BODY_OFFSET; }
        public boolean animatesHead() { return DANCE_HEAD_ANIMATION; }
    };

    /** Сон: стенд лежит на спине и дышит (Sleep.bbmodel). Кость body здесь тоже двигается (дыхание). */
    private static final BoneClip SLEEP_CLIP = new BoneClip() {
        public float[] rotation(int bone, float time) { return SleepAnimation.rotation(bone, time); }
        public float[] position(int bone, float time) { return SleepAnimation.position(bone, time); }
        public float[] bodyOffset(float time) { return SleepAnimation.position(SleepAnimation.BODY, time); }
        public boolean animatesHead() { return true; }
    };

    private static float[] danceRotation(BoneClip clip, int bone, float time) {
        float[] r = clip.rotation(bone, time);
        return MIRROR_DANCE ? new float[] { r[0], -r[1], -r[2] } : r;
    }

    private static float[] dancePosition(BoneClip clip, int bone, float time) {
        float[] p = clip.position(bone, time);
        return MIRROR_DANCE ? new float[] { -p[0], p[1], p[2] } : p;
    }

    // Кость Stand из Blockbench - корень всей модели: её поворот и смещение накладываются на модель целиком (renderToBuffer)
    private boolean danceRootActive = false;
    private final float[] danceRootRotation = new float[3];
    private final float[] danceRootPosition = new float[3];
    private boolean positionsModified = false;

    private void captureBasePositions() {
        ModelRenderer[] parts = { body, upperPart, head, torso, rightArm, leftArm, rightLeg, leftLeg, skull, chest1, chest2, barHang, heart };
        for (ModelRenderer part : parts) {
            if (part != null && !basePositions.containsKey(part)) {
                basePositions.put(part, new float[] { part.x, part.y, part.z });
            }
        }
    }

    /** Возвращает смещённые анимацией части на их исходные места (после танца или Chara). */
    private void restoreBasePositions() {
        if (!positionsModified) {
            return;
        }
        positionsModified = false;
        for (Map.Entry<ModelRenderer, float[]> entry : basePositions.entrySet()) {
            ModelRenderer part = entry.getKey();
            float[] base = entry.getValue();
            part.x = base[0];
            part.y = base[1];
            part.z = base[2];
        }
    }

    /** Поворот вектора в Blockbench: сначала вокруг X, затем Y, затем Z (градусы). */
    private static float[] rotateVector(float x, float y, float z, float[] rot) {
        double rx = Math.toRadians(rot[0]), ry = Math.toRadians(rot[1]), rz = Math.toRadians(rot[2]);
        double cx = Math.cos(rx), sx = Math.sin(rx);
        double y1 = y * cx - z * sx;
        double z1 = y * sx + z * cx;
        double cy = Math.cos(ry), sy = Math.sin(ry);
        double x2 = x * cy + z1 * sy;
        double z2 = -x * sy + z1 * cy;
        double cz = Math.cos(rz), sz = Math.sin(rz);
        double x3 = x2 * cz - y1 * sz;
        double y3 = x2 * sz + y1 * cz;
        return new float[] { (float) x3, (float) y3, (float) z2 };
    }

    /**
     * Играет танец в момент time (секунды от начала). Все кости в Dance.bbmodel вращаются вокруг точки (0, 26, 0),
     * а части игровой модели - вокруг своих точек, поэтому смещение пересчитывается:
     * T' = T + (P - Q) - R * (P - Q), где P - точка вращения в Blockbench, Q - точка вращения части в игре.
     *
     * Оси: игра рисует кубики из geo так же, как в Blockbench по X и Z, но ось Y у неё направлена вниз
     * (y_игры = -y_Blockbench). Поэтому поза из Blockbench переносится как отражение по Y:
     * углы (x, y, z) -> (-x, y, -z), смещения (x, y, z) -> (x, -y, z).
     * (У анимаций Chara другие знаки - там данные были подготовлены иначе, их трогать не нужно.)
     */
    private void applyDance(float time) {
        applyBoneClip(DANCE_CLIP, time);
    }

    /** Во сколько раз сон играется медленнее, чем в Sleep.bbmodel (2-3 раза: дыхание длится 2.5 с вместо 1 с). */
    private static final float SLEEP_SLOWDOWN = 2.5F;

    /** Сон: то же самое, что танец, но с данными из Sleep.bbmodel. */
    private void applySleep(float time) {
        applyBoneClip(SLEEP_CLIP, time);
    }

    private void applyBoneClip(BoneClip clip, float time) {
        positionsModified = true;

        // Корень (Stand) - на всю модель
        danceRootActive = true;
        float[] rootRot = danceRotation(clip, DanceAnimation.STAND, time);
        float[] rootPos = dancePosition(clip, DanceAnimation.STAND, time);
        System.arraycopy(rootRot, 0, danceRootRotation, 0, 3);
        System.arraycopy(rootPos, 0, danceRootPosition, 0, 3);

        // Талия и тело своих анимаций в танце не имеют
        ModelRenderer[] still = { body, upperPart };
        for (ModelRenderer part : still) {
            if (part != null) {
                part.xRot = 0.0F;
                part.yRot = 0.0F;
                part.zRot = 0.0F;
            }
        }
        // В Blockbench "Right arm" - рука на +X, а в игровой модели это leftArm (и так же с ногами).
        // При зеркальном танце стороны меняются местами.
        int leftArmBone = MIRROR_DANCE ? DanceAnimation.LEFT_ARM : DanceAnimation.RIGHT_ARM;
        int rightArmBone = MIRROR_DANCE ? DanceAnimation.RIGHT_ARM : DanceAnimation.LEFT_ARM;
        int leftLegBone = MIRROR_DANCE ? DanceAnimation.LEFT_LEG : DanceAnimation.RIGHT_LEG;
        int rightLegBone = MIRROR_DANCE ? DanceAnimation.RIGHT_LEG : DanceAnimation.LEFT_LEG;
        // Без анимации головы остаётся только постоянное смещение (0, -1, 1), как у тела
        applyDancePart(clip, head, clip.animatesHead() ? DanceAnimation.HEAD : -1, PIVOT_HEAD, time, !clip.animatesHead());
        applyDancePart(clip, torso, -1, PIVOT_TORSO, time, true);
        applyDancePart(clip, leftArm, leftArmBone, PIVOT_LEFT_ARM, time, true);
        applyDancePart(clip, rightArm, rightArmBone, PIVOT_RIGHT_ARM, time, true);
        applyDancePart(clip, leftLeg, leftLegBone, PIVOT_LEFT_LEG, time, false);
        applyDancePart(clip, rightLeg, rightLegBone, PIVOT_RIGHT_LEG, time, false);
    }

    private void applyDancePart(BoneClip clip, ModelRenderer part, int bone, float[] pivot, float time, boolean underBody) {
        if (part == null) {
            return;
        }
        float[] rot = bone >= 0 ? danceRotation(clip, bone, time) : new float[3];
        float[] pos = bone >= 0 ? dancePosition(clip, bone, time) : new float[3];
        float tx = pos[0], ty = pos[1], tz = pos[2];
        if (underBody) {
            // Кость body в Blockbench сдвинута (в танце на постоянные (0, -1, 1), во сне "дышит") и несёт торс и руки
            float[] bodyRaw = clip.bodyOffset(time);
            float bodyX = MIRROR_DANCE ? -bodyRaw[0] : bodyRaw[0];
            tx += bodyX;
            ty += bodyRaw[1];
            tz += bodyRaw[2];
        }
        float vx = DANCE_PIVOT[0] - pivot[0];
        float vy = DANCE_PIVOT[1] - pivot[1];
        float vz = DANCE_PIVOT[2] - pivot[2];
        float[] rv = rotateVector(vx, vy, vz, rot);
        tx += vx - rv[0];
        ty += vy - rv[1];
        tz += vz - rv[2];

        part.xRot = (float) Math.toRadians(-rot[0]);
        part.yRot = (float) Math.toRadians(rot[1]);
        part.zRot = (float) Math.toRadians(-rot[2]);
        float[] base = basePositions.get(part);
        if (base != null) {
            part.x = base[0] + tx;
            part.y = base[1] - ty;
            part.z = base[2] + tz;
        }
    }

    /** Поворот и смещение кости Stand на всю модель: вокруг точки (0, 26, 0) из Blockbench. */
    private void applyDanceRootTransform(MatrixStack matrixStack) {
        if (!danceRootActive) {
            return;
        }
        float[] base = basePositions.get(body);
        if (base == null) {
            return;
        }
        // Точка (0, 26, 0) Blockbench в осях игровой модели: корень body стоит в (0, 23, 0), разница (0, 3, 0) -> (0, -3, 0)
        float px = base[0] / 16.0F;
        float py = (base[1] - (DANCE_PIVOT[1] - PIVOT_BODY[1])) / 16.0F;
        float pz = base[2] / 16.0F;
        matrixStack.translate(px + danceRootPosition[0] / 16.0F, py - danceRootPosition[1] / 16.0F, pz + danceRootPosition[2] / 16.0F);
        matrixStack.mulPose(Vector3f.ZP.rotation((float) Math.toRadians(-danceRootRotation[2])));
        matrixStack.mulPose(Vector3f.YP.rotation((float) Math.toRadians(danceRootRotation[1])));
        matrixStack.mulPose(Vector3f.XP.rotation((float) Math.toRadians(-danceRootRotation[0])));
        matrixStack.translate(-px, -py, -pz);
    }

    // ======================= Танец Flowey (анимация из Flowery_dance.bbmodel) =======================
    /**
     * В Flowery_dance.bbmodel модель нарисована на 7.5 единиц левее, чем в geo скина (а анимация кости stand возвращает её
     * на 8 обратно). Чтобы цветок встал ровно, к смещению каждой части добавляется эта разница.
     */
    private static final float FLOWEY_LAYOUT_SHIFT = 7.5F;

    /**
     * Играет танец Flowey в момент time (секунды, 0-1). Части двигаются тем же способом, что у Chara:
     * углы (-x, -y, z), смещения (-x, -y, z) из Blockbench. Точка вращения стебля chest1 задана в geo.
     */
    private void applyFloweyDance(float time) {
        positionsModified = true;
        if (head != null) {
            head.xRot = 0.0F;
            head.yRot = 0.0F;
            head.zRot = 0.0F;
        }
        float[] stand = FloweyDanceAnimation.position(FloweyDanceAnimation.STAND, time);
        placeFloweyPart(skull, FloweyDanceAnimation.HEAD, time, stand);
        placeFloweyPart(chest1, FloweyDanceAnimation.CHEST1, time, stand);
        placeFloweyPart(chest2, FloweyDanceAnimation.CHEST2, time, stand);
    }

    private void placeFloweyPart(ModelRenderer part, int bone, float time, float[] stand) {
        if (part == null) {
            return;
        }
        float[] rot = FloweyDanceAnimation.rotation(bone, time);
        float[] pos = FloweyDanceAnimation.position(bone, time);
        float tx = pos[0] + stand[0];
        float ty = pos[1] + stand[1];
        float tz = pos[2] + stand[2];
        part.xRot = (float) Math.toRadians(-rot[0]);
        part.yRot = (float) Math.toRadians(-rot[1]);
        part.zRot = (float) Math.toRadians(rot[2]);
        float[] base = basePositions.get(part);
        if (base != null) {
            part.x = base[0] - tx + FLOWEY_LAYOUT_SHIFT;
            part.y = base[1] - ty;
            part.z = base[2] + tz;
        }
    }

    /** Спокойная стойка: руки и ноги опущены, лёгкое дыхание. Для скинов на блоке и для обычного после танца. */
    private void applyStandingPose(float ticks) {
        ModelRenderer[] still = { body, upperPart, torso, leftLeg, rightLeg };
        for (ModelRenderer part : still) {
            if (part != null) {
                part.xRot = 0.0F;
                part.yRot = 0.0F;
                part.zRot = 0.0F;
            }
        }
        float breathe = 0.03F * MathHelper.sin(ticks * 0.1F);
        if (leftArm != null) {
            leftArm.xRot = 0.0F;
            leftArm.yRot = 0.0F;
            leftArm.zRot = -breathe;
        }
        if (rightArm != null) {
            rightArm.xRot = 0.0F;
            rightArm.yRot = 0.0F;
            rightArm.zRot = breathe;
        }
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
