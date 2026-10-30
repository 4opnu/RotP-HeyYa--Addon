package com.rotpaddon.heyya;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rotpaddon.heyya.init.InitEffects;
import com.rotpaddon.heyya.init.InitEntities;
import com.rotpaddon.heyya.init.InitItems;
import com.rotpaddon.heyya.init.InitSounds;
import com.rotpaddon.heyya.init.InitStands;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// Главный класс аддона Hey Ya!
@Mod(AddonMain.MOD_ID)
public class AddonMain {
    // Должен совпадать с modId в META-INF/mods.toml
    public static final String MOD_ID = "heyya";
    public static final Logger LOGGER = LogManager.getLogger();

    public AddonMain() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        InitEntities.ENTITIES.register(modEventBus);
        InitItems.ITEMS.register(modEventBus);
        InitEffects.EFFECTS.register(modEventBus);
        InitSounds.SOUNDS.register(modEventBus);
        InitStands.ACTIONS.register(modEventBus);
        InitStands.STANDS.register(modEventBus);
    }
}
