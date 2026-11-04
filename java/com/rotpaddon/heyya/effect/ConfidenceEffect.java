package com.rotpaddon.heyya.effect;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

/** Уверенность - полезный эффект: пока он действует, паника на игрока не накладывается. */
public class ConfidenceEffect extends Effect {

    public ConfidenceEffect() {
        super(EffectType.BENEFICIAL, 0xE8C23A);
    }
}
