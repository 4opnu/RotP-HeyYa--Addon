package com.rotpaddon.heyya.item;

import java.util.Collections;
import java.util.Random;

import com.rotpaddon.heyya.AddonMain;
import com.rotpaddon.heyya.init.InitItems;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Что можно найти по способности "Брось это!":
 *  50% - булыжник (метательный, 2 урона)
 *  40% - нож из RotP (jojo:knife)
 *   5% - ядовитое взрывное зелье (яд на 15 секунд)
 *   4% - железный топор на 1 использование
 *   1% - камень Аджи (jojo:aja_stone)
 */
public class PickupLoot {
    private static final int POISON_TICKS = 15 * 20;

    public static ItemStack roll(Random random) {
        int r = random.nextInt(100);      // 0..99
        if (r < 50) {
            return new ItemStack(InitItems.THROWABLE_COBBLESTONE.get());
        }
        if (r < 90) {
            return fromRegistry("knife");
        }
        if (r < 95) {
            return poisonSplashPotion();
        }
        if (r < 99) {
            return singleUseIronAxe();
        }
        return fromRegistry("aja_stone");
    }

    /** Предмет оригинального мода по id jojo:<name>. Если не найден - выдаём булыжник, чтобы не остаться без награды. */
    private static ItemStack fromRegistry(String name) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("jojo", name));
        if (item == null || item == Items.AIR) {
            AddonMain.LOGGER.warn("Hey Ya!: предмет jojo:{} не найден, вместо него выдан булыжник", name);
            return new ItemStack(InitItems.THROWABLE_COBBLESTONE.get());
        }
        return new ItemStack(item);
    }

    private static ItemStack poisonSplashPotion() {
        ItemStack potion = new ItemStack(Items.SPLASH_POTION);
        PotionUtils.setCustomEffects(potion,
                Collections.singletonList(new EffectInstance(Effects.POISON, POISON_TICKS, 0)));
        potion.setHoverName(new TranslationTextComponent("item.heyya.poison_splash_potion")
                .withStyle(style -> style.withItalic(false)));
        return potion;
    }

    private static ItemStack singleUseIronAxe() {
        ItemStack axe = new ItemStack(Items.IRON_AXE);
        axe.setDamageValue(axe.getMaxDamage() - 1);     // остаётся одно использование
        return axe;
    }
}
