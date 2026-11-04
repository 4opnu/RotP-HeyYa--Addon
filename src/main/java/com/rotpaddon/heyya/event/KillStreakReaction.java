package com.rotpaddon.heyya.event;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.rotpaddon.heyya.AddonMain;
import com.rotpaddon.heyya.entity.HeyYaEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Серия убийств: если владелец убил KILLS_REQUIRED мобов за WINDOW_TICKS (15 мобов за 7 минут), призванный стенд
 * это комментирует (Hey Ya!: "Да ты и правда силён!", Flowey: "Убей их всех!", Chara: "А ты напоминаешь меня... хех").
 * После реплики счётчик обнуляется - следующая реплика будет после новых 15 убийств.
 * Считаются только убийства, когда стенд призван. Тексты - chat.heyya.<hey_ya|flowey|chara>_adv.kill_streak.
 */
@Mod.EventBusSubscriber(modid = AddonMain.MOD_ID)
public class KillStreakReaction {
    public static final int KILLS_REQUIRED = 15;
    public static final int WINDOW_TICKS = 7 * 60 * 20;
    /** true - считать только враждебных мобов, false - любых мобов (в том числе мирных животных). */
    private static final boolean HOSTILE_ONLY = false;

    /** Время (игровой тик) недавних убийств каждого игрока. */
    private static final Map<UUID, Deque<Long>> RECENT_KILLS = new HashMap<>();

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity killer = event.getSource().getEntity();
        LivingEntity victim = event.getEntityLiving();
        if (!(killer instanceof PlayerEntity) || !(victim instanceof MobEntity)) {
            return;
        }
        PlayerEntity player = (PlayerEntity) killer;
        if (player.level.isClientSide() || (HOSTILE_ONLY && !(victim instanceof IMob))) {
            return;
        }
        if (!PanicHandler.isHeyYaUser(player)) {
            return;
        }
        HeyYaEntity stand = AdvancementReactions.findStand(player);
        if (stand == null) {
            return;
        }

        long now = player.level.getGameTime();
        Deque<Long> kills = RECENT_KILLS.computeIfAbsent(player.getUUID(), id -> new ArrayDeque<>());
        kills.addLast(now);
        while (!kills.isEmpty() && now - kills.peekFirst() > WINDOW_TICKS) {
            kills.removeFirst();
        }
        if (kills.size() >= KILLS_REQUIRED) {
            kills.clear();
            stand.sayAdvancement("kill_streak");
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        RECENT_KILLS.remove(event.getPlayer().getUUID());
    }
}
