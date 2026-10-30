package com.rotpaddon.heyya.effect;

import com.rotpaddon.heyya.init.InitSounds;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.SoundCategory;

/**
 * Паника - вредный эффект. Затемнение экрана и надписи рисует клиент (client/PanicOverlay),
 * а на сервере эффект проигрывает тяжёлое дыхание, пока паника не пройдёт.
 */
public class PanicEffect extends Effect {
    /** Запись дыхания длится 5.4 секунды (108 тиков), следующий вдох включаем сразу после неё. */
    private static final int BREATH_INTERVAL_TICKS = 108;
    private static final String LAST_BREATH_TAG = "heyya_panic_breath_time";

    public PanicEffect() {
        super(EffectType.HARMFUL, 0x5A0A0A);
    }

    // Проверяем раз в секунду, пора ли включать дыхание
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level.isClientSide()) {
            return;
        }
        long now = entity.level.getGameTime();
        long last = entity.getPersistentData().getLong(LAST_BREATH_TAG);
        if (now < last || now - last >= BREATH_INTERVAL_TICKS) {
            entity.getPersistentData().putLong(LAST_BREATH_TAG, now);
            // Звук привязан к игроку и идёт вместе с ним
            entity.level.playSound(null, entity,
                    InitSounds.HEY_YA_PANIC_BREATH.get(), SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
    }
}
