package com.rotpaddon.heyya.entity;

import com.rotpaddon.heyya.init.InitEntities;
import com.rotpaddon.heyya.init.InitItems;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

/** Брошенный булыжник: ровно 2 урона любому существу (в том числе игроку). */
public class ThrownCobblestoneEntity extends ProjectileItemEntity {
    public static final float DAMAGE = 2.0F;

    /**
     * Урон булыжника. Игнорирует броню и чары защиты, иначе по закованной в броню цели удар проходил
     * почти без урона (то самое "иногда не наносит урон").
     */
    private static class CobblestoneDamage extends IndirectEntityDamageSource {
        CobblestoneDamage(Entity projectile, Entity owner) {
            super("thrown", projectile, owner);
            setProjectile();
            bypassArmor();
            bypassMagic();
        }
    }

    public ThrownCobblestoneEntity(EntityType<? extends ThrownCobblestoneEntity> type, World world) {
        super(type, world);
    }

    public ThrownCobblestoneEntity(World world, LivingEntity thrower) {
        super(InitEntities.THROWN_COBBLESTONE.get(), thrower, world);
    }

    @Override
    protected Item getDefaultItem() {
        return InitItems.THROWABLE_COBBLESTONE.get();
    }

    // Камень пролетает сквозь находку (она тоже "кликабельна") и сквозь собственный стенд владельца,
    // иначе он исчезал, ударившись о них, и урон не наносился
    @Override
    protected boolean canHitEntity(Entity entity) {
        if (entity instanceof HeyYaPickupEntity) {
            return false;
        }
        if (entity instanceof HeyYaEntity && ((HeyYaEntity) entity).getUser() == getOwner()) {
            return false;
        }
        return super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityRayTraceResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        if (target instanceof LivingEntity) {
            // Без этого удар сразу после другого урона (кадры неуязвимости) проходил с нулевым уроном
            target.invulnerableTime = 0;
        }
        DamageSource source = new CobblestoneDamage(this, getOwner());
        target.hurt(source, DAMAGE);
    }

    @Override
    protected void onHit(RayTraceResult result) {
        super.onHit(result);
        if (!level.isClientSide()) {
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.STONE_BREAK, SoundCategory.NEUTRAL, 0.8F, 1.0F);
            remove();
        }
    }
}
