package com.rotpaddon.heyya.client;

import com.rotpaddon.heyya.AddonMain;
import com.rotpaddon.heyya.client.render.HeyYaPickupRenderer;
import com.rotpaddon.heyya.client.render.HeyYaRenderer;
import com.rotpaddon.heyya.client.render.OreGlowRenderer;
import com.rotpaddon.heyya.init.InitEntities;
import com.rotpaddon.heyya.init.InitStands;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = AddonMain.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientInit {

    @SubscribeEvent
    public static void onFMLClientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(
                InitStands.STAND_HEY_YA.getEntityType(), HeyYaRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(
                InitEntities.PICKUP.get(), HeyYaPickupRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(
                InitEntities.ORE_GLOW.get(), OreGlowRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(
                InitEntities.THROWN_COBBLESTONE.get(),
                manager -> new SpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
    }
}
