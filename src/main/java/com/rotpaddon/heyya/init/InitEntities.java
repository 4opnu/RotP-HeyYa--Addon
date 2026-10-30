package com.rotpaddon.heyya.init;

import com.rotpaddon.heyya.AddonMain;
import com.rotpaddon.heyya.entity.HeyYaPickupEntity;
import com.rotpaddon.heyya.entity.OreGlowEntity;
import com.rotpaddon.heyya.entity.ThrownCobblestoneEntity;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitEntities {
    // Сущность самого стенда регистрируется в InitStands (через EntityStandRegistryObject).
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(
            ForgeRegistries.ENTITIES, AddonMain.MOD_ID);

    // Маленький светящийся квадрат-находка ("Брось это!")
    public static final RegistryObject<EntityType<HeyYaPickupEntity>> PICKUP = ENTITIES.register("hey_ya_pickup",
            () -> EntityType.Builder.<HeyYaPickupEntity>of(HeyYaPickupEntity::new, EntityClassification.MISC)
            .sized(0.7F, 0.7F)
            .clientTrackingRange(8)
            .updateInterval(20)
            .fireImmune()
            .noSave()
            .build(AddonMain.MOD_ID + ":hey_ya_pickup"));

    // Брошенный булыжник
    public static final RegistryObject<EntityType<ThrownCobblestoneEntity>> THROWN_COBBLESTONE = ENTITIES.register("thrown_cobblestone",
            () -> EntityType.Builder.<ThrownCobblestoneEntity>of(ThrownCobblestoneEntity::new, EntityClassification.MISC)
            .sized(0.25F, 0.25F)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build(AddonMain.MOD_ID + ":thrown_cobblestone"));

    // Светящийся блок руды ("Ты пропустил это!")
    public static final RegistryObject<EntityType<OreGlowEntity>> ORE_GLOW = ENTITIES.register("ore_glow",
            () -> EntityType.Builder.<OreGlowEntity>of(OreGlowEntity::new, EntityClassification.MISC)
            .sized(0.1F, 0.1F)
            .clientTrackingRange(8)
            .updateInterval(20)
            .fireImmune()
            .noSave()
            .build(AddonMain.MOD_ID + ":ore_glow"));
}
