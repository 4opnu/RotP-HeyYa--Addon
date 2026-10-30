package com.rotpaddon.heyya.init;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.init.power.stand.EntityStandRegistryObject;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.type.EntityStandType;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.rotpaddon.heyya.AddonMain;
import com.rotpaddon.heyya.action.HeyYaCalmDown;
import com.rotpaddon.heyya.action.HeyYaMotivation;
import com.rotpaddon.heyya.action.HeyYaOreSense;
import com.rotpaddon.heyya.action.HeyYaThrowThis;
import com.rotpaddon.heyya.action.HeyYaTalk;
import com.rotpaddon.heyya.action.HeyYaWatchBehind;
import com.rotpaddon.heyya.entity.HeyYaEntity;

import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class InitStands {
    @SuppressWarnings("unchecked")
    public static final DeferredRegister<Action<?>> ACTIONS = DeferredRegister.create(
            (Class<Action<?>>) ((Class<?>) Action.class), AddonMain.MOD_ID);
    @SuppressWarnings("unchecked")
    public static final DeferredRegister<StandType<?>> STANDS = DeferredRegister.create(
            (Class<StandType<?>>) ((Class<?>) StandType.class), AddonMain.MOD_ID);

    // ======================================== Hey Ya! ========================================

    // Способность 1: поговорить (случайная фраза из 50 про Майнкрафт), без перезарядки
    public static final RegistryObject<StandEntityAction> HEY_YA_TALK = ACTIONS.register("hey_ya_talk",
            () -> new HeyYaTalk(new StandEntityAction.Builder()
                    .standRecoveryTicks(40)));

    // Способность 2: мотивация (фраза + Скорость I, Спешка I, Сила I на 10 секунд)
    public static final RegistryObject<StandEntityAction> HEY_YA_MOTIVATION = ACTIONS.register("hey_ya_motivation",
            () -> new HeyYaMotivation(new StandEntityAction.Builder()
                    .staminaCost(100)
                    .standRecoveryTicks(100)
                    .cooldown(30 * 20)));       // перезарядка 30 секунд

    // Способность 3: осторожно сзади (переключатель вкл/выкл), без перезарядки
    public static final RegistryObject<StandEntityAction> HEY_YA_WATCH_BEHIND = ACTIONS.register("hey_ya_watch_behind",
            () -> new HeyYaWatchBehind(new StandEntityAction.Builder()
                    .standRecoveryTicks(20)));

    // Способность 4: ты пропустил это (подсветка руд в радиусе 6 блоков), перезарядка 20 сек
    public static final RegistryObject<StandEntityAction> HEY_YA_ORE_SENSE = ACTIONS.register("hey_ya_ore_sense",
            () -> new HeyYaOreSense(new StandEntityAction.Builder()
                    .staminaCost(50)
                    .standRecoveryTicks(100)
                    .cooldown(20 * 20)));       // перезарядка 20 секунд

    // Способность 5 (атакующая, 1-й ряд): "Брось это!" - светящаяся находка в 4-5 блоках от игрока
    public static final RegistryObject<StandEntityAction> HEY_YA_THROW_THIS = ACTIONS.register("hey_ya_throw_this",
            () -> new HeyYaThrowThis(new StandEntityAction.Builder()
                    .staminaCost(150)
                    .standRecoveryTicks(60)
                    .cooldown(25 * 20)));       // перезарядка 25 секунд

    // Способность 6 (2-й ряд): "Успокойся" - убирает Панику и даёт Уверенность (только при здоровье меньше 50%).
    // Перезарядка 75 сек считается внутри HeyYaCalmDown и стартует только после УДАЧНОГО использования.
    public static final RegistryObject<StandEntityAction> HEY_YA_CALM_DOWN = ACTIONS.register("hey_ya_calm_down",
            () -> new HeyYaCalmDown(new StandEntityAction.Builder()
                    .standRecoveryTicks(40)));

    public static final EntityStandRegistryObject<EntityStandType<StandStats>, StandEntityType<HeyYaEntity>> STAND_HEY_YA =
            new EntityStandRegistryObject<>("hey_ya",
                    STANDS,
                    () -> new EntityStandType.Builder<StandStats>()
                    .color(0xF5C542)
                    .storyPartName(ModStandsInit.PART_7_NAME)
                    .leftClickHotbar(
                            HEY_YA_TALK.get(),
                            HEY_YA_MOTIVATION.get(),
                            HEY_YA_THROW_THIS.get()
                            )
                    .rightClickHotbar(
                            HEY_YA_WATCH_BEHIND.get(),
                            HEY_YA_ORE_SENSE.get(),
                            HEY_YA_CALM_DOWN.get()
                            )
                    .defaultStats(StandStats.class, new StandStats.Builder()
                            .tier(1)
                            .power(1)           // E
                            .speed(1)           // E
                            .range(1.5, 3)      // E
                            .durability(12)     // B (выносливость: ранг B при значении 11..13.99)
                            .precision(1)       // E
                            .build())
                    .build(),

                    InitEntities.ENTITIES,
                    () -> new StandEntityType<HeyYaEntity>(HeyYaEntity::new, 0.4F, 0.9F)
                    .summonSound(InitSounds.HEY_YA_SUMMON_SOUND)
                    .unsummonSound(InitSounds.HEY_YA_UNSUMMON_SOUND))
            .withDefaultStandAttributes();
}
