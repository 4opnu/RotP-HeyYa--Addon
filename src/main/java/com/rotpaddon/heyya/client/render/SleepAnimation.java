package com.rotpaddon.heyya.client.render;

/**
 * Анимация сна Hey Ya!, перенесённая из Blockbench (файл Sleep.bbmodel, анимация sleep, 1 с).
 * Играется по кругу, пока владелец спит в кровати: стенд лежит на спине на животе игрока и "дышит".
 * Данные - ключевые кадры из Blockbench, интерполяция линейная. Применяет HeyYaModel (тем же способом, что и танец).
 *
 * Кость STAND - поворот и смещение всей модели (поворот по Y на 90 и по Z на 295: стенд ложится на спину),
 * BODY - дыхание: смещение тела вместе с руками (голова - отдельная кость и дышит своими кадрами).
 */
final class SleepAnimation {
    static final int STAND = 0, HEAD = 1, RIGHT_ARM = 2, LEFT_ARM = 3, RIGHT_LEG = 4, LEFT_LEG = 5, BODY = 6;

    /** Длина анимации в секундах. */
    static final float LENGTH = 1.0F;

    private static CharaAnimations.Channel ch(float[] times, float[][] values) {
        return new CharaAnimations.Channel(times, values);
    }

    static final CharaAnimations.Channel[] ROTATION = {
        /* STAND     */ ch(new float[] {0F, 1F}, new float[][] {{0F, 90F, 295F}, {0F, 90F, 295F}}),
        /* HEAD      */ ch(new float[] {0F, 0.5F, 1F}, new float[][] {{0F, 0F, 0F}, {-5F, 0F, 0F}, {0F, 0F, 0F}}),
        /* RIGHT_ARM */ ch(new float[] {0F, 0.5F, 1F}, new float[][] {{189.3464F, -39.0762F, 9.2004F}, {181.8464F, -39.0762F, 9.2004F}, {189.3464F, -39.0762F, 9.2004F}}),
        /* LEFT_ARM  */ ch(new float[] {0F, 0.5F, 1F}, new float[][] {{188.4987F, 32.2419F, -8.4985F}, {183.4987F, 32.2419F, -8.4985F}, {188.4987F, 32.2419F, -8.4985F}}),
        /* RIGHT_LEG */ ch(new float[] {0F, 0.5F, 1F}, new float[][] {{0F, 0F, -27.5F}, {2.5F, 0F, -27.5F}, {0F, 0F, -27.5F}}),
        /* LEFT_LEG  */ ch(new float[] {0F, 0.5F, 1F}, new float[][] {{11.7679F, -4.2454F, 19.5621F}, {16.7679F, -4.2454F, 19.5621F}, {11.7679F, -4.2454F, 19.5621F}}),
        /* BODY      */ null
    };

    static final CharaAnimations.Channel[] POSITION = {
        /* STAND     */ ch(new float[] {0F, 1F}, new float[][] {{5F, -20F, 0F}, {5F, -20F, 0F}}),
        /* HEAD      */ ch(new float[] {0F, 0.5F, 1F}, new float[][] {{0F, 0F, 0F}, {0F, 0F, -1F}, {0F, 0F, 0F}}),
        /* RIGHT_ARM */ ch(new float[] {0F, 1F}, new float[][] {{3F, -10F, -3F}, {3F, -10F, -3F}}),
        /* LEFT_ARM  */ ch(new float[] {0F, 1F}, new float[][] {{-2F, -11F, -3F}, {-2F, -11F, -3F}}),
        /* RIGHT_LEG */ ch(new float[] {0F, 1F}, new float[][] {{8F, 1F, -2F}, {8F, 1F, -2F}}),
        /* LEFT_LEG  */ ch(new float[] {0F, 1F}, new float[][] {{-5F, 2F, 2F}, {-5F, 2F, 2F}}),
        /* BODY      */ ch(new float[] {0F, 0.5F, 1F}, new float[][] {{0F, 0F, 0F}, {0F, 0F, -1F}, {0F, 0F, 0F}})
    };

    private static final float[] ZERO = {0F, 0F, 0F};

    static float[] rotation(int bone, float time) {
        return ROTATION[bone] == null ? ZERO : ROTATION[bone].at(time);
    }

    static float[] position(int bone, float time) {
        return POSITION[bone] == null ? ZERO : POSITION[bone].at(time);
    }

    private SleepAnimation() {}
}
