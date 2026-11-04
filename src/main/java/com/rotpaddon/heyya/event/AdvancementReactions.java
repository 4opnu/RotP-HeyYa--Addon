package com.rotpaddon.heyya.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rotpaddon.heyya.AddonMain;
import com.rotpaddon.heyya.entity.HeyYaEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Стенд Hey Ya! комментирует некоторые достижения владельца (если стенд сейчас призван):
 *  - Isn't It Iron Pick (story/iron_tools)      - железная кирка: прочный и полезный инструмент
 *  - Hot Stuff (story/lava_bucket)              - осторожнее с этим
 *  - Diamonds! (story/mine_diamond)             - мы нашли их!
 *  - Cover Me with Diamonds (story/shiny_gear)  - настоящий красавец
 *  - The End? (story/enter_the_end)             - вот и финальный босс
 *  - Nether (nether/root)                       - тут как-то жарко
 *  - Monster Hunter (adventure/kill_a_mob)      - да ты силён
 * Тексты - chat.heyya.<hey_ya|flowey|chara>_adv.<ключ> в lang-файлах (у каждого скина свои).
 */
@Mod.EventBusSubscriber(modid = AddonMain.MOD_ID)
public class AdvancementReactions {
    /** id достижения Minecraft -> ключ реакции. */
    private static final Map<String, String> REACTIONS = new HashMap<>();
    static {
        REACTIONS.put("minecraft:story/iron_tools", "iron_tools");
        REACTIONS.put("minecraft:story/lava_bucket", "hot_stuff");
        REACTIONS.put("minecraft:story/mine_diamond", "diamonds");
        REACTIONS.put("minecraft:story/shiny_gear", "cover_me");
        REACTIONS.put("minecraft:story/enter_the_end", "the_end");
        REACTIONS.put("minecraft:nether/root", "nether");
        REACTIONS.put("minecraft:adventure/kill_a_mob", "monster_hunter");
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player.level.isClientSide()) {
            return;
        }
        String key = REACTIONS.get(event.getAdvancement().getId().toString());
        if (key == null || !PanicHandler.isHeyYaUser(player)) {
            return;
        }
        HeyYaEntity stand = findStand(player);
        if (stand != null) {
            stand.sayAdvancement(key);
        }
    }

    /**
     * Призванный стенд этого игрока: ищем рядом (он стоит на спине, на блоке для танца - не дальше 32 блоков - или лежит на животе).
     * @return null, если стенд сейчас не призван
     */
    static HeyYaEntity findStand(PlayerEntity player) {
        List<HeyYaEntity> stands = player.level.getEntitiesOfClass(HeyYaEntity.class,
                player.getBoundingBox().inflate(64.0), stand -> stand.getUser() == player);
        return stands.isEmpty() ? null : stands.get(0);
    }
}
