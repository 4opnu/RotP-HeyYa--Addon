package com.rotpaddon.heyya.init;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.ModSounds;
import com.rotpaddon.heyya.AddonMain;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(
            ForgeRegistries.SOUND_EVENTS, AddonMain.MOD_ID);

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

    // Тяжёлое дыхание во время Паники
    public static final RegistryObject<SoundEvent> HEY_YA_PANIC_BREATH = register("hey_ya_panic_breath");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> new SoundEvent(new ResourceLocation(AddonMain.MOD_ID, name)));
    }
}
