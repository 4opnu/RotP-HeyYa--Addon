package com.rotpaddon.heyya.event;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.heyya.AddonMain;
import com.rotpaddon.heyya.init.InitEffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Накладывает дебаффы страха на ВЛАДЕЛЬЦА стенда Hey Ya! (даже если стенд не призван):
 *  - Дрожащие руки: урон, после которого осталось меньше 70% здоровья; 40 секунд.
 *    Пока эффект действует, при новом уроне есть шанс уронить предмет из руки.
 *  - Паника: урон, после которого осталось меньше 30% здоровья; 30 секунд.
 * Пока действует Уверенность (способность "Успокойся"), новые дебаффы не накладываются.
 * Игроки без стенда и владельцы других стендов дебаффы не получают.
 */
@Mod.EventBusSubscriber(modid = AddonMain.MOD_ID)
public class PanicHandler {
    private static final ResourceLocation HEY_YA_STAND_ID = new ResourceLocation(AddonMain.MOD_ID, "hey_ya");

    /** Паника: здоровья после удара меньше этой доли. */
    public static final float HEALTH_THRESHOLD = 0.3F;
    /** Сколько длится Паника после последнего урона (30 секунд). */
    public static final int PANIC_TICKS = 30 * 20;

    /** Дрожащие руки: здоровья после удара меньше этой доли. */
    public static final float TREMBLING_HEALTH_THRESHOLD = 0.7F;
    /** Сколько длятся Дрожащие руки (40 секунд). */
    public static final int TREMBLING_TICKS = 40 * 20;
    /** Шанс уронить предмет при каждом новом уроне, пока действуют Дрожащие руки. */
    public static final float DROP_CHANCE = 0.4F;

    /** Является ли сущность владельцем именно стенда Hey Ya! (призван он или нет - не важно). */
    public static boolean isHeyYaUser(LivingEntity entity) {
        return IStandPower.getStandPowerOptional(entity)
                .map(power -> power.getType() != null
                        && HEY_YA_STAND_ID.equals(power.getType().getRegistryName()))
                .orElse(false);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (!(entity instanceof PlayerEntity) || entity.level.isClientSide() || event.getAmount() <= 0F) {
            return;
        }
        if (!isHeyYaUser(entity)) {
            return;
        }
        float healthAfter = entity.getHealth() - event.getAmount();
        if (healthAfter <= 0F) {
            return;     // удар смертельный - дебаффы уже не нужны
        }

        // 1) Дрожащие руки уже действуют (повторный урон) - может выпасть предмет
        if (entity.hasEffect(InitEffects.TREMBLING_HANDS.get())
                && entity.level.random.nextFloat() < DROP_CHANCE) {
            dropHeldItem((PlayerEntity) entity);
        }

        // 2) Новые дебаффы - только если нет Уверенности
        if (entity.hasEffect(InitEffects.CONFIDENCE.get())) {
            return;
        }
        float maxHealth = entity.getMaxHealth();
        if (healthAfter < maxHealth * TREMBLING_HEALTH_THRESHOLD) {
            entity.addEffect(new EffectInstance(InitEffects.TREMBLING_HANDS.get(), TREMBLING_TICKS, 0, false, true, true));
        }
        if (healthAfter < maxHealth * HEALTH_THRESHOLD) {
            entity.addEffect(new EffectInstance(InitEffects.PANIC.get(), PANIC_TICKS, 0, false, true, true));
        }
    }

    /** Игрок роняет предмет из основной руки (если рука не пустая). */
    private static void dropHeldItem(PlayerEntity player) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            return;
        }
        ItemStack dropped = held.copy();
        player.setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        player.drop(dropped, false);
        player.displayClientMessage(
                new TranslationTextComponent("chat.heyya.trembling_drop").withStyle(TextFormatting.GRAY), true);
    }
}
