package com.rotpaddon.heyya.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Подсветка руды: невидимая сущность с эффектом свечения (Glowing), которая рисует копию блока руды
 * ровно на месте самой руды. Контур светящегося блока виден сквозь стены. Исчезает сама через заданное время.
 */
public class OreGlowEntity extends Entity {
    private static final DataParameter<Integer> ORE_STATE =
            EntityDataManager.defineId(OreGlowEntity.class, DataSerializers.INT);

    private int lifetimeTicks = 100;

    public OreGlowEntity(EntityType<?> type, World world) {
        super(type, world);
        noPhysics = true;
        setGlowing(true);
    }

    /** Какой блок рисовать и сколько тиков светиться. Вызывать на сервере сразу после создания. */
    public void setup(BlockState oreState, int lifetimeTicks) {
        entityData.set(ORE_STATE, Block.getId(oreState));
        this.lifetimeTicks = lifetimeTicks;
    }

    public BlockState getOreState() {
        return Block.stateById(entityData.get(ORE_STATE));
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(ORE_STATE, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {}

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {}

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    // У сущности крошечный хитбокс, поэтому расширяем область, по которой игра решает, рисовать ли её
    @Override
    public AxisAlignedBB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(1.0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide() && tickCount >= lifetimeTicks) {
            remove();
        }
    }
}
