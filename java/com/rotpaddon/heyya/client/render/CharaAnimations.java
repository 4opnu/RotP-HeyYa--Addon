package com.rotpaddon.heyya.client.render;

/**
 * Анимации скина Chara, перенесённые из Blockbench (файл chara_skin_2.bbmodel): Summon (при призыве) и Idle (после неё, по кругу).
 * Данные созданы скриптом из ключевых кадров, значения в Blockbench: градусы и единицы модели, интерполяция линейная.
 * Порядок костей: Waist (upperPart), Head, Body (torso), Right Arm, Left Arm, Right Leg, Left Leg.
 */
final class CharaAnimations {
    static final int WAIST = 0, HEAD = 1, BODY = 2, RIGHT_ARM = 3, LEFT_ARM = 4, RIGHT_LEG = 5, LEFT_LEG = 6;

    /** Длина анимации Summon в секундах. */
    static final float SUMMON_LENGTH = 1.125F;
    /**
     * Длина цикла Idle. В Blockbench у Idle длина 3 секунды, но движение повторяется каждые 2 секунды
     * (кадры в 0 и 2 с совпадают), поэтому для бесшовной зацикленности берём 2 секунды.
     */
    static final float IDLE_LOOP_LENGTH = 2.0F;

    /** Базовый поворот костей в Blockbench (градусы), к нему добавляется поворот из анимации. */
    static final float[][] REST_ROTATION = {
        {0F, 0F, 0F},
        {6F, -5F, 0F},
        {0F, 0F, 0F},
        {10F, 0F, 0F},
        {-12F, 0F, 0F},
        {-11F, 0F, 2F},
        {10F, 0F, -2F}
    };

    static final class Channel {
        private final float[] times;
        private final float[][] values;

        Channel(float[] times, float[][] values) {
            this.times = times;
            this.values = values;
        }

        /** Значение канала в момент time (секунды): линейная интерполяция, до первого и после последнего кадра значение держится. */
        float[] at(float time) {
            int n = times.length;
            if (time <= times[0]) return values[0];
            if (time >= times[n - 1]) return values[n - 1];
            int i = 1;
            while (times[i] < time) i++;
            float k = (time - times[i - 1]) / (times[i] - times[i - 1]);
            float[] a = values[i - 1];
            float[] b = values[i];
            return new float[] { a[0] + (b[0] - a[0]) * k, a[1] + (b[1] - a[1]) * k, a[2] + (b[2] - a[2]) * k };
        }
    }

    private static final float[] ZERO = {0F, 0F, 0F};

    static final class Clip {
        private final Channel[] rotation;
        private final Channel[] position;

        Clip(Channel[] rotation, Channel[] position) {
            this.rotation = rotation;
            this.position = position;
        }

        float[] rotation(int bone, float time) {
            return rotation[bone] == null ? ZERO : rotation[bone].at(time);
        }

        float[] position(int bone, float time) {
            return position[bone] == null ? ZERO : position[bone].at(time);
        }
    }

    private static Channel ch(float[] times, float[][] values) {
        return new Channel(times, values);
    }

    static final Clip SUMMON = new Clip(
        new Channel[] {
            null,
            ch(new float[] {0F, 0.625F, 0.875F, 1.125F}, new float[][] {{70F, 0F, 0F}, {260F, 0F, 0F}, {335F, 0F, 0F}, {340F, 0F, 0F}}),
            ch(new float[] {0F, 0.25F, 0.625F, 0.875F, 1.125F}, new float[][] {{42.5F, 0F, 0F}, {65F, 0F, 0F}, {242.5F, 0F, 0F}, {317.5F, 0F, 0F}, {310F, 0F, 0F}}),
            ch(new float[] {0F, 0.25F, 0.5F, 0.75F, 1.125F}, new float[][] {{0F, 0F, 0F}, {135.8221F, -35.955F, -73.4285F}, {199.0928F, -22.6484F, -36.6478F}, {288.1969F, -9.3419F, 0.1329F}, {350.6969F, -9.3419F, 0.1329F}}),
            ch(new float[] {0F, 0.25F, 0.5F, 0.75F, 1.125F}, new float[][] {{0F, 0F, 0F}, {174.6492F, 36.7305F, 93.808F}, {193.7736F, 19.5762F, 46.0735F}, {327.8979F, 2.4219F, -1.661F}, {380.3979F, 2.4219F, -1.661F}}),
            ch(new float[] {0F, 0.25F, 0.5F, 0.75F}, new float[][] {{0F, 0F, 0F}, {70F, 0F, 0F}, {160F, 0F, 0F}, {267.5F, 0F, 0F}}),
            ch(new float[] {0F, 0.25F, 0.5F, 0.75F}, new float[][] {{0F, 0F, 0F}, {33.75F, 0F, 0F}, {117.5F, 0F, 0F}, {242.5F, 0F, 0F}})},
        new Channel[] {
            ch(new float[] {0F, 0.25F, 0.625F, 0.875F, 1.125F}, new float[][] {{0F, 0F, 0F}, {0F, 0F, 5F}, {0F, 0F, 5F}, {0F, 0F, -2F}, {0F, 0F, -8F}}),
            ch(new float[] {0F, 0.625F, 1.125F}, new float[][] {{0F, 0F, 7F}, {0F, 0F, 7F}, {0F, -2F, 6F}}),
            ch(new float[] {0F, 1.125F}, new float[][] {{0F, 0F, 7F}, {0F, -2F, 6F}}),
            ch(new float[] {0F, 0.5F, 1.125F}, new float[][] {{0F, 0F, 5F}, {0F, 2.15F, 6.28F}, {0F, -2F, 8F}}),
            ch(new float[] {0F, 0.5F, 1.125F}, new float[][] {{0F, 0F, 6F}, {0F, 1.15F, 6.85F}, {0F, -2F, 8F}}),
            ch(new float[] {0F, 0.25F, 0.375F, 0.5F, 0.625F, 0.75F, 0.875F, 1F, 1.125F}, new float[][] {{0F, 4F, -2F}, {0F, 7.26F, 0.85F}, {0F, 18.82F, 2.78F}, {0F, 22.38F, 12.7F}, {0F, 15.04F, 22.23F}, {0F, 7.7F, 19.75F}, {0F, 1.86F, 12.45F}, {0F, 2.01F, 9.14F}, {0F, 0.67F, 5.33F}}),
            ch(new float[] {0F, 0.25F, 0.375F, 0.5F, 0.625F, 0.75F, 0.875F, 1F, 1.125F}, new float[][] {{0F, 4F, -2F}, {0F, 6.26F, 0.93F}, {0F, 20.18F, 4.75F}, {0F, 22.09F, 16.57F}, {0F, 14.81F, 22.99F}, {0F, 7.52F, 19.41F}, {0F, 1.73F, 12.33F}, {0F, 1.95F, 9.25F}, {0F, 0.67F, 5.67F}})});

    static final Clip IDLE = new Clip(
        new Channel[] {
            null,
            ch(new float[] {0F, 1F, 2F, 3F}, new float[][] {{-7.5F, 0F, 0F}, {-15F, 0F, 0F}, {-7.5F, 0F, 0F}, {-15F, 0F, 0F}}),
            ch(new float[] {0F, 2F}, new float[][] {{-47.5F, 0F, 0F}, {-47.5F, 0F, 0F}}),
            ch(new float[] {0F, 1F, 2F, 3F}, new float[][] {{-12.5F, 0F, 0F}, {-12.4148F, 0.6468F, 14.9863F}, {-12.5F, 0F, 0F}, {-12.4148F, 0.6468F, 14.9863F}}),
            ch(new float[] {0F, 1F, 2F, 3F}, new float[][] {{10F, 0F, 0F}, {10.0304F, -0.3472F, -9.994F}, {10F, 0F, 0F}, {10.0304F, -0.3472F, -9.994F}}),
            ch(new float[] {0F, 1F, 2F, 3F}, new float[][] {{-80F, 0F, 0F}, {-92.5F, 0F, 0F}, {-80F, 0F, 0F}, {-92.5F, 0F, 0F}}),
            ch(new float[] {0F, 1F, 2F, 3F}, new float[][] {{-100F, 0F, 0F}, {-112.5F, 0F, 0F}, {-100F, 0F, 0F}, {-112.5F, 0F, 0F}})},
        new Channel[] {
            ch(new float[] {0F, 1F, 2F, 3F}, new float[][] {{0F, 0F, 0F}, {0F, -2F, 0F}, {0F, 0F, 0F}, {0F, -2F, 0F}}),
            ch(new float[] {0F, 2F}, new float[][] {{0F, 0F, -1F}, {0F, 0F, -1F}}),
            null,
            ch(new float[] {0F, 2F}, new float[][] {{0F, 0F, 2F}, {0F, 0F, 2F}}),
            ch(new float[] {0F, 2F}, new float[][] {{0F, 0F, 2F}, {0F, 0F, 2F}}),
            ch(new float[] {0F, 1F, 2F, 3F}, new float[][] {{0F, 1F, 6F}, {0F, -1F, 6F}, {0F, 1F, 6F}, {0F, -1F, 6F}}),
            ch(new float[] {0F, 1F, 2F, 3F}, new float[][] {{0F, 1F, 6F}, {0F, -1F, 6F}, {0F, 1F, 6F}, {0F, -1F, 6F}})});

    private CharaAnimations() {}
}
