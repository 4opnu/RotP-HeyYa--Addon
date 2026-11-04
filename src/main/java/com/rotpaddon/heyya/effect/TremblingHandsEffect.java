package com.rotpaddon.heyya.effect;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

/**
 * Дрожащие руки - вредный эффект. Сам по себе ничего не делает: пока он висит на игроке,
 * при каждом новом уроне есть шанс уронить предмет из руки (логика в event/PanicHandler).
 */
public class TremblingHandsEffect extends Effect {

    public TremblingHandsEffect() {
        super(EffectType.HARMFUL, 0x8A6A4A);
    }
}
