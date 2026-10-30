package com.rotpaddon.heyya.init;

import com.rotpaddon.heyya.AddonMain;
import com.rotpaddon.heyya.item.ThrowableCobblestoneItem;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
            ForgeRegistries.ITEMS, AddonMain.MOD_ID);

    // Булыжник, который можно бросить по ПКМ (2 урона)
    public static final RegistryObject<Item> THROWABLE_COBBLESTONE = ITEMS.register("throwable_cobblestone",
            () -> new ThrowableCobblestoneItem(new Item.Properties().tab(ItemGroup.TAB_COMBAT).stacksTo(16)));
}
