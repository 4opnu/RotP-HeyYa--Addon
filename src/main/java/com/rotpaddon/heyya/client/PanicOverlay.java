package com.rotpaddon.heyya.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.rotpaddon.heyya.AddonMain;
import com.rotpaddon.heyya.init.InitEffects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Экранные эффекты Паники (по мотивам мода Sanity): затемнение краёв экрана с пульсацией,
 * красноватый оттенок и множество дрожащих надписей вроде "Я умру!" и "Убегай!".
 * Рисуется только у игрока, на котором висит эффект Паника.
 */
@Mod.EventBusSubscriber(modid = AddonMain.MOD_ID, value = Dist.CLIENT)
public class PanicOverlay {
    private static final int PHRASES_AVAILABLE = 6;     // panic.heyya.phrase.1 ... .6
    private static final int MAX_PHRASES = 14;
    // Только тёмно-красные оттенки (без белого и розового)
    private static final int[] COLORS = { 0x8B0000, 0x7F0000, 0x990000, 0x8A0808 };

    private static final Random RANDOM = new Random();
    private static final List<Phrase> PHRASES = new ArrayList<>();
    private static int ticks = 0;
    private static float intensity = 0F;    // 0..1, плавно нарастает и затухает

    private static class Phrase {
        String text;
        float x, y, scale, angle;
        int rgb, age, life;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        boolean panic = player != null && player.hasEffect(InitEffects.PANIC.get());

        intensity = panic ? Math.min(1F, intensity + 0.05F) : Math.max(0F, intensity - 0.04F);
        ticks++;

        Iterator<Phrase> it = PHRASES.iterator();
        while (it.hasNext()) {
            Phrase p = it.next();
            if (++p.age >= p.life) {
                it.remove();
            }
        }

        if (panic && PHRASES.size() < MAX_PHRASES && RANDOM.nextInt(5) == 0) {
            Phrase p = new Phrase();
            p.text = I18n.get("panic.heyya.phrase." + (1 + RANDOM.nextInt(PHRASES_AVAILABLE)));
            p.scale = 1.0F + RANDOM.nextFloat() * 2.2F;
            p.x = 0.04F + RANDOM.nextFloat() * 0.66F;
            p.y = 0.08F + RANDOM.nextFloat() * 0.80F;
            p.angle = (RANDOM.nextFloat() - 0.5F) * 30F;
            p.rgb = COLORS[RANDOM.nextInt(COLORS.length)];
            p.life = 30 + RANDOM.nextInt(45);
            PHRASES.add(p);
        }
        if (!panic && intensity <= 0F) {
            PHRASES.clear();
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL || intensity <= 0F) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        MatrixStack stack = event.getMatrixStack();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        float partial = event.getPartialTicks();
        float time = ticks + partial;
        float pulse = 0.5F + 0.5F * MathHelper.sin(time * 0.12F);

        // 1) общее лёгкое затемнение и красноватый оттенок
        AbstractGui.fill(stack, 0, 0, w, h, ((int) (intensity * (105 + 25 * pulse)) << 24));
        AbstractGui.fill(stack, 0, 0, w, h, ((int) (intensity * (8 + 10 * pulse)) << 24) | 0x500000);

        // 2) тёмная виньетка по краям, пульсирует как сердцебиение
        drawVignette(stack, w, h, intensity * (0.85F + 0.15F * pulse));

        // 3) надписи
        for (Phrase p : PHRASES) {
            float t = (p.age + partial) / p.life;
            float alpha = t < 0.2F ? t / 0.2F : (t > 0.7F ? (1F - t) / 0.3F : 1F);
            int a = (int) (alpha * 255F * intensity);
            if (a < 8) {
                continue;
            }
            float shakeX = (RANDOM.nextFloat() - 0.5F) * 2.0F;
            float shakeY = (RANDOM.nextFloat() - 0.5F) * 2.0F;
            stack.pushPose();
            stack.translate(p.x * w + shakeX, p.y * h + shakeY, 0.0D);
            stack.mulPose(Vector3f.ZP.rotationDegrees(p.angle));
            stack.scale(p.scale, p.scale, 1.0F);
            mc.font.drawShadow(stack, p.text, 0.0F, 0.0F, (a << 24) | p.rgb);
            stack.popPose();
        }
    }

    /** Виньетка: вложенные рамки со слабой прозрачностью дают плавное затемнение к краям. */
    private static void drawVignette(MatrixStack stack, int w, int h, float strength) {
        final int layers = 14;
        int alpha = (int) (strength * 255F * 1.8F / layers);
        if (alpha <= 0) {
            return;
        }
        int color = alpha << 24;
        for (int i = 1; i <= layers; i++) {
            int tx = (int) (w * 0.40F * i / layers);
            int ty = (int) (h * 0.40F * i / layers);
            AbstractGui.fill(stack, 0, 0, w, ty, color);                 // сверху
            AbstractGui.fill(stack, 0, h - ty, w, h, color);             // снизу
            AbstractGui.fill(stack, 0, ty, tx, h - ty, color);           // слева
            AbstractGui.fill(stack, w - tx, ty, w, h - ty, color);       // справа
        }
    }
}
