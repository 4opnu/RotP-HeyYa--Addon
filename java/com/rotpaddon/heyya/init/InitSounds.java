package com.rotpaddon.heyya.init;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.util.mc.OstSoundList;
import com.rotpaddon.heyya.AddonMain;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(
            ForgeRegistries.SOUND_EVENTS, AddonMain.MOD_ID);

    // OST Hey Ya!: RotP сам включает его, когда у владельца стенда появляется эффект Решимости.
    // OstSoundList регистрирует события hey_ya_ost_30, _60, _75, _90, _120 и _full (по длине Решимости).
    public static final OstSoundList HEY_YA_OST = new OstSoundList(new ResourceLocation(AddonMain.MOD_ID, "hey_ya_ost"), SOUNDS);

    // Призыв/развоплощение: стандартные звуки RotP
    public static final Supplier<SoundEvent> HEY_YA_SUMMON_SOUND = ModSounds.STAND_SUMMON_DEFAULT;

    public static final Supplier<SoundEvent> HEY_YA_UNSUMMON_SOUND = ModSounds.STAND_UNSUMMON_DEFAULT;

    // Голос Hey Ya! Файлы лежат в assets/heyya/sounds/*.ogg, привязка - в assets/heyya/sounds.json
    public static final RegistryObject<SoundEvent> HEY_YA_TALK_1 = register("hey_ya_talk_1");
    public static final RegistryObject<SoundEvent> HEY_YA_TALK_2 = register("hey_ya_talk_2");
    public static final RegistryObject<SoundEvent> HEY_YA_ORE_SENSE = register("hey_ya_ore_sense");
    public static final RegistryObject<SoundEvent> HEY_YA_DANGER = register("hey_ya_danger");
    // Озвучка скина "Цветок" (нарезана из одной записи): 3 реплики разговора, находка руды, опасность
    public static final RegistryObject<SoundEvent> HEY_YA_FLOWEY_TALK_1 = register("hey_ya_flowey_talk_1");
    public static final RegistryObject<SoundEvent> HEY_YA_FLOWEY_TALK_2 = register("hey_ya_flowey_talk_2");
    public static final RegistryObject<SoundEvent> HEY_YA_FLOWEY_TALK_3 = register("hey_ya_flowey_talk_3");
    public static final RegistryObject<SoundEvent> HEY_YA_FLOWEY_ORE = register("hey_ya_flowey_ore");
    public static final RegistryObject<SoundEvent> HEY_YA_FLOWEY_DANGER = register("hey_ya_flowey_danger");

    // Озвучка скина "Chara": 3 реплики разговора, находка руды и опасность (две последние - начало записей)
    public static final RegistryObject<SoundEvent> HEY_YA_CHARA_TALK_1 = register("hey_ya_chara_talk_1");
    public static final RegistryObject<SoundEvent> HEY_YA_CHARA_TALK_2 = register("hey_ya_chara_talk_2");
    public static final RegistryObject<SoundEvent> HEY_YA_CHARA_TALK_3 = register("hey_ya_chara_talk_3");
    public static final RegistryObject<SoundEvent> HEY_YA_CHARA_ORE = register("hey_ya_chara_ore");
    public static final RegistryObject<SoundEvent> HEY_YA_CHARA_DANGER = register("hey_ya_chara_danger");

    /** Реплика разговора скина "Chara" по номеру 0..2. */
    public static SoundEvent charaTalk(int index) {
        switch (index) {
        case 0: return HEY_YA_CHARA_TALK_1.get();
        case 1: return HEY_YA_CHARA_TALK_2.get();
        default: return HEY_YA_CHARA_TALK_3.get();
        }
    }

    /** Реплика разговора скина "Цветок" по номеру 0..2. */
    public static SoundEvent floweyTalk(int index) {
        switch (index) {
        case 0: return HEY_YA_FLOWEY_TALK_1.get();
        case 1: return HEY_YA_FLOWEY_TALK_2.get();
        default: return HEY_YA_FLOWEY_TALK_3.get();
        }
    }

    // Музыка танца (assets/heyya/sounds/dance_music.ogg, 12.7 с): играет по кругу, пока стенд танцует
    public static final RegistryObject<SoundEvent> HEY_YA_DANCE_MUSIC = register("hey_ya_dance_music");

    // Музыка танца Flowey (assets/heyya/sounds/flowey_dance_music.ogg, 23.3 с)
    public static final RegistryObject<SoundEvent> FLOWEY_DANCE_MUSIC = register("flowey_dance_music");

    // Музыка танца Chara (assets/heyya/sounds/chara_dance_music.ogg, 22.06 с)
    public static final RegistryObject<SoundEvent> CHARA_DANCE_MUSIC = register("chara_dance_music");

    // Тяжёлое дыхание во время Паники
    public static final RegistryObject<SoundEvent> HEY_YA_PANIC_BREATH = register("hey_ya_panic_breath");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> new SoundEvent(new ResourceLocation(AddonMain.MOD_ID, name)));
    }
}
