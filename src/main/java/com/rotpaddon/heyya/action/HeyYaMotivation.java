package com.rotpaddon.heyya.action;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.heyya.entity.HeyYaEntity;
import com.rotpaddon.heyya.init.InitEffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

// Способность 2: мотивирующая фраза + Скорость I, Спешка I и Сила I на несколько секунд.
public class HeyYaMotivation extends StandEntityAction {
    // Сколько мотивирующих фраз лежит в lang-файлах (chat.heyya.hey_ya_motivation.1 ... .8)
    public static final int PHRASES_COUNT = 8;
    // 10 секунд. Уровень I = амплитуда 0.
    private static final int EFFECT_DURATION_TICKS = 20 * 10;
    private static final int EFFECT_AMPLIFIER = 0;

    public HeyYaMotivation(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            if (standEntity instanceof HeyYaEntity) {
                ((HeyYaEntity) standEntity).sayTalk();
            }

            LivingEntity user = userPower.getUser();
            if (user != null) {
                user.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, EFFECT_DURATION_TICKS, EFFECT_AMPLIFIER));
                user.addEffect(new EffectInstance(Effects.DIG_SPEED, EFFECT_DURATION_TICKS, EFFECT_AMPLIFIER));
                user.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, EFFECT_DURATION_TICKS, EFFECT_AMPLIFIER));
                // Мотивация успокаивает дрожь в руках
                user.removeEffect(InitEffects.TREMBLING_HANDS.get());

                if (user instanceof PlayerEntity) {
                    TranslationTextComponent message = standEntity instanceof HeyYaEntity
                            ? ((HeyYaEntity) standEntity).motivationMessage()
                            : new TranslationTextComponent("chat.heyya.hey_ya_motivation." + (world.random.nextInt(PHRASES_COUNT) + 1));
                    ((PlayerEntity) user).displayClientMessage(message.withStyle(TextFormatting.GREEN), false);
                }
            }
        }
    }
}
