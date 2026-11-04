package com.rotpaddon.heyya.client.render;

/**
 * Анимация танца Flowey, перенесённая из Blockbench (файл Flowery_dance.bbmodel, анимация Dance, 1 с).
 * В игре играется по кругу, пока стенд танцует. Данные - ключевые кадры из Blockbench, интерполяция линейная.
 *
 * Кости: STAND - смещение всей модели (цветок опускается и стоит на блоке), HEAD - голова с лепестками
 * (кость Hair в файле двигается точно так же, поэтому отдельной не нужно), CHEST1 - стебель (качается вокруг оси Z),
 * CHEST2 - верхняя часть стебля. Применяет HeyYaModel#applyFloweyDance.
 */
final class FloweyDanceAnimation {
    static final int STAND = 0, HEAD = 1, CHEST1 = 2, CHEST2 = 3;

    /** Длина анимации в секундах. */
    static final float LENGTH = 1.0F;

    private static CharaAnimations.Channel ch(float[] times, float[][] values) {
        return new CharaAnimations.Channel(times, values);
    }

    static final CharaAnimations.Channel[] ROTATION = {
        /* STAND  */ null,
        /* HEAD   */ null,
        /* CHEST1 */ ch(new float[] {0F, 0.25F, 0.5F, 0.75F, 1F}, new float[][] {{0F, 0F, -90F}, {0F, 0F, -75F}, {0F, 0F, -90F}, {0F, 0F, -107.5F}, {0F, 0F, -90F}}),
        /* CHEST2 */ null
    };

    static final CharaAnimations.Channel[] POSITION = {
        /* STAND  */ ch(new float[] {0F, 0.25F, 0.5F, 0.75F, 1F}, new float[][] {{8F, -13F, 0F}, {8F, -15F, 0F}, {8F, -13F, 0F}, {9F, -15F, 0F}, {8F, -13F, 0F}}),
        /* HEAD   */ ch(new float[] {0F, 0.25F, 0.5F, 0.75F, 1F}, new float[][] {{0F, 0F, 0F}, {-1F, 0F, 0F}, {0F, 0F, 0F}, {3F, 0F, 0F}, {0F, 0F, 0F}}),
        /* CHEST1 */ ch(new float[] {0F, 0.5F, 1F}, new float[][] {{-2F, -6F, 0F}, {-2F, -6F, 0F}, {-2F, -6F, 0F}}),
        /* CHEST2 */ ch(new float[] {0F, 0.25F, 0.5F, 0.75F, 1F}, new float[][] {{0F, 0F, 0F}, {-1F, -1F, 0F}, {0F, 0F, 0F}, {2F, 0F, 0F}, {0F, 0F, 0F}})
    };

    private static final float[] ZERO = {0F, 0F, 0F};

    static float[] rotation(int bone, float time) {
        return ROTATION[bone] == null ? ZERO : ROTATION[bone].at(time);
    }

    static float[] position(int bone, float time) {
        return POSITION[bone] == null ? ZERO : POSITION[bone].at(time);
    }

    private FloweyDanceAnimation() {}
}
