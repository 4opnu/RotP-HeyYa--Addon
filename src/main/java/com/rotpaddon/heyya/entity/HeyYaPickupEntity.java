package com.rotpaddon.heyya.entity;

import com.rotpaddon.heyya.item.PickupLoot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Булыжник, наполовину торчащий из земли. ПКМ - подобрать случайный предмет (см. PickupLoot).
 * Исчезает сам через минуту.
 */
public class HeyYaPickupEntity extends Entity {
    private static final int LIFETIME_TICKS = 20 * 60;

    /** Контур свечения вокруг булыжника (виден и сквозь стены). По умолчанию выключен: подсветка и так заметна. */
    private static final boolean GLOW_OUTLINE = false;

    public HeyYaPickupEntity(EntityType<?> type, World world) {
        super(type, world);
        if (GLOW_OUTLINE) {
            setGlowing(true);
        }
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {}

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {}

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;      // не ломается от ударов
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide()) {
            // Небольшая подсветка: редкие светлые искры над камнем
            if (tickCount % 8 == 0) {
                level.addParticle(ParticleTypes.END_ROD,
                        getX() + (random.nextDouble() - 0.5) * 0.35, getY() + 0.15 + random.nextDouble() * 0.25,
                        getZ() + (random.nextDouble() - 0.5) * 0.35, 0.0, 0.01, 0.0);
            }
        }
        else if (tickCount > LIFETIME_TICKS) {
            remove();
        }
    }

    @Override
    public ActionResultType interact(PlayerEntity player, Hand hand) {
        if (hand != Hand.MAIN_HAND) {
            return ActionResultType.PASS;
        }
        if (!level.isClientSide()) {
            ItemStack stack = PickupLoot.roll(level.random);
            ItemStack shown = stack.copy();
            if (!player.inventory.add(stack)) {
                player.drop(stack, false);
            }
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.ITEM_PICKUP, SoundCategory.PLAYERS, 0.6F, 1.2F);
            player.displayClientMessage(
                    new TranslationTextComponent("chat.heyya.hey_ya_pickup", shown.getHoverName())
                    .withStyle(TextFormatting.AQUA), true);
            remove();
        }
        return ActionResultType.sidedSuccess(level.isClientSide());
    }
}
