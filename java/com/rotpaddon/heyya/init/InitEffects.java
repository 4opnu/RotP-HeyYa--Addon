package com.rotpaddon.heyya.init;

import com.rotpaddon.heyya.AddonMain;
import com.rotpaddon.heyya.effect.ConfidenceEffect;
import com.rotpaddon.heyya.effect.PanicEffect;
import com.rotpaddon.heyya.effect.TremblingHandsEffect;

import net.minecraft.potion.Effect;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitEffects {
    // В Forge 1.16.5 реестр эффектов называется POTIONS
    public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(
            ForgeRegistries.POTIONS, AddonMain.MOD_ID);

    public static final RegistryObject<Effect> PANIC = EFFECTS.register("panic", PanicEffect::new);
    public static final RegistryObject<Effect> CONFIDENCE = EFFECTS.register("confidence", ConfidenceEffect::new);
    public static final RegistryObject<Effect> TREMBLING_HANDS = EFFECTS.register("trembling_hands", TremblingHandsEffect::new);
}
