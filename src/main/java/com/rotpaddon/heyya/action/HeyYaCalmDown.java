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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/**
 * "Успокойся" (2-й ряд): убирает Панику и даёт Уверенность на 1 минуту (в это время паника не накладывается).
 * Работает только когда у игрока меньше 50% здоровья.
 */
public class HeyYaCalmDown extends StandEntityAction {
    /** Фразы стенда: chat.heyya.hey_ya_calm.1 ... .3 */
    public static final int PHRASES_COUNT = 3;
    /** Доля здоровья, ниже которой способность работает. */
    public static final float HEALTH_THRESHOLD = 0.5F;
    public static final int CONFIDENCE_TICKS = 60 * 20;
    /** Перезарядка 75 секунд. Стартует только когда способность реально сработала. */
    public static final int COOLDOWN_TICKS = 75 * 20;
    private static final String READY_TIME_TAG = "heyya_calm_ready_time";

    public HeyYaCalmDown(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (world.isClientSide()) {
            return;
        }
        LivingEntity user = userPower.getUser();
        if (user == null) {
            return;
        }
        PlayerEntity player = user instanceof PlayerEntity ? (PlayerEntity) user : null;

        // Нельзя использовать, пока здоровья 50% и больше
        if (user.getHealth() >= user.getMaxHealth() * HEALTH_THRESHOLD) {
            if (player != null) {
                player.displayClientMessage(
                        new TranslationTextComponent("chat.heyya.hey_ya_calm.not_needed")
                        .withStyle(TextFormatting.GRAY), true);
            }
            return;
        }

        // Перезарядка (если на этом игроке она ещё идёт - ничего не делаем)
        long now = world.getGameTime();
        long readyAt = user.getPersistentData().getLong(READY_TIME_TAG);
        if (now < readyAt && readyAt - now <= COOLDOWN_TICKS) {
            if (player != null) {
                long secondsLeft = (readyAt - now + 19) / 20;
                player.displayClientMessage(
                        new TranslationTextComponent("chat.heyya.hey_ya_calm.cooldown", secondsLeft)
                        .withStyle(TextFormatting.GRAY), true);
            }
            return;
        }
        user.getPersistentData().putLong(READY_TIME_TAG, now + COOLDOWN_TICKS);

        user.removeEffect(InitEffects.PANIC.get());
        user.removeEffect(InitEffects.TREMBLING_HANDS.get());
        user.addEffect(new EffectInstance(InitEffects.CONFIDENCE.get(), CONFIDENCE_TICKS, 0, false, true, true));

        if (player != null) {
            TranslationTextComponent message = standEntity instanceof HeyYaEntity
                    ? ((HeyYaEntity) standEntity).calmMessage()
                    : new TranslationTextComponent("chat.heyya.hey_ya_calm." + (world.random.nextInt(PHRASES_COUNT) + 1));
            player.displayClientMessage(message.withStyle(TextFormatting.GREEN), false);
        }
        if (standEntity instanceof HeyYaEntity) {
            ((HeyYaEntity) standEntity).sayTalk();
        }
    }
}
