package com.rotpaddon.heyya.entity;

import java.util.List;
import java.util.UUID;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.heyya.AddonMain;
import com.rotpaddon.heyya.init.InitEntities;
import com.rotpaddon.heyya.init.InitSounds;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SStopSoundPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class HeyYaEntity extends StandEntity {
    // ======================= Подвеска на спине (всё настраивается здесь) =======================
    /** Расстояние за спиной игрока (в блоках). */
    public static final double BACK_DISTANCE = 0.40;
    /** Сдвиг вбок. Один знак - к одному плечу, другой - к другому. Если стенд оказался не на той стороне - смените знак. */
    public static final double SIDE_OFFSET = 0.25;
    /** Высота ног стенда над ногами игрока. Больше - стенд выше на спине. */
    public static final double HANG_HEIGHT = 0.65;
    /**
     * Множитель размера модели при отрисовке. RotP сам уменьшает стенд ещё примерно вдвое,
     * поэтому 1.15 в игре даёт стенд ростом примерно 1.1 блока. Увеличьте число - стенд станет больше.
     */
    public static final float MODEL_SCALE = 1.15F;

    // ======================= Скины =======================
    public static final int SKIN_DEFAULT = 0;
    public static final int SKIN_FLOWEY = 1;
    public static final int SKIN_CHARA = 2;
    /** Id скина "Цветок" в системе скинов RotP (assets/heyya/jojo_stand_skins.json). */
    private static final ResourceLocation FLOWEY_SKIN_ID = new ResourceLocation(AddonMain.MOD_ID, "flowey");
    private static final ResourceLocation CHARA_SKIN_ID = new ResourceLocation(AddonMain.MOD_ID, "chara");
    /** Скин "Цветок": лицо стоит на плече, поэтому он выше и чуть дальше от спины, чем обычный стенд. */
    public static final double FLOWEY_BACK_DISTANCE = 0.36;

    // Скин "Chara": стенд не висит на игроке, а парит на уровне головы чуть в стороне и сзади.
    /** Расстояние за спиной игрока. Больше - дальше от тела. */
    public static final double CHARA_BACK_DISTANCE = 0.65;
    /** Сдвиг вправо от центра игрока. */
    public static final double CHARA_SIDE_OFFSET = 0.50;
    /** Высота точки отсчёта стенда над ногами игрока: при 0.61 голова стенда (в позе из анимации Idle) на уровне головы игрока. */
    public static final double CHARA_HOVER_HEIGHT = 0.61;
    /** Дополнительное покачивание сущности вверх-вниз (в блоках). 0 - покачивание даёт только анимация Idle. */
    public static final double CHARA_BOB_AMPLITUDE = 0.0;
    /** Множитель размера скина (чуть больше, чем у обычного стенда: модель выше, 32 ед. против 29). */
    public static final float CHARA_MODEL_SCALE = 1.15F;
    public static final double FLOWEY_HANG_HEIGHT = 0.57;
    /** Сдвиг к правому плечу у скина (у обычного стенда SIDE_OFFSET). Больше - правее. */
    public static final double FLOWEY_SIDE_OFFSET = 0.22;
    // Озвучка скина "Цветок": длина записи в тиках + запас на затухание
    private static final int FLOWEY_TALK_1_TICKS = 36 + 6;
    private static final int FLOWEY_TALK_2_TICKS = 54 + 6;
    private static final int FLOWEY_TALK_3_TICKS = 82 + 6;
    private static final int FLOWEY_ORE_TICKS = 10 + 6;
    private static final int FLOWEY_DANGER_TICKS = 30 + 6;
    // Озвучка скина "Chara": 1.85 c, 2.76 c, 4.15 c, руда 0.5 c, опасность 1.5 c (+ запас на затухание)
    private static final int CHARA_TALK_1_TICKS = 37 + 6;
    private static final int CHARA_TALK_2_TICKS = 55 + 6;
    private static final int CHARA_TALK_3_TICKS = 83 + 6;
    private static final int CHARA_ORE_TICKS = 10 + 6;
    private static final int CHARA_DANGER_TICKS = 30 + 6;
    /**
     * Ширина стенда относительно исходной модели (1.0 = как в Blockbench, 0.8 = на 20% уже).
     * Рост и глубина не меняются, текстура остаётся прежней.
     */
    public static final float MODEL_WIDTH_SCALE = 0.8F;

    // ======================= Разговор =======================
    /** Сколько обычных фраз лежит в lang-файлах (chat.heyya.hey_ya_talk.1 ... .50). */
    public static final int TALK_PHRASES_COUNT = 50;
    /** Сколько фраз-приветствий при призыве (chat.heyya.hey_ya_summon.1 ... .3). */
    public static final int SUMMON_PHRASES_COUNT = 3;
    /** Пассивная реплика раз в 3 минуты (в тиках). */
    public static final int PASSIVE_TALK_INTERVAL_TICKS = 3 * 60 * 20;

    // Игровое время (в тиках), до которого стенд "говорит". Синхронизируется клиенту.
    private static final DataParameter<Integer> TALK_UNTIL =
            EntityDataManager.defineId(HeyYaEntity.class, DataSerializers.INT);
    // Номер скина (синхронизируется клиенту, чтобы рендерер выбрал нужную модель)
    private static final DataParameter<Integer> SKIN =
            EntityDataManager.defineId(HeyYaEntity.class, DataSerializers.INT);
    // Танец: стоит ли стенд на блоке, игровое время начала и точка, где он стоит (синхронизируется клиенту)
    private static final DataParameter<Boolean> DANCING =
            EntityDataManager.defineId(HeyYaEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> DANCE_START =
            EntityDataManager.defineId(HeyYaEntity.class, DataSerializers.INT);
    private static final DataParameter<Float> DANCE_X =
            EntityDataManager.defineId(HeyYaEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> DANCE_Y =
            EntityDataManager.defineId(HeyYaEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> DANCE_Z =
            EntityDataManager.defineId(HeyYaEntity.class, DataSerializers.FLOAT);

    // Длительность анимации = длина озвучки (в тиках) + небольшой запас на затухание.
    // talk_1 = 1.80 c, talk_2 = 2.16 c, ore_sense = 0.50 c, danger = 1.51 c
    private static final int TALK_1_TICKS = 36 + 6;
    private static final int TALK_2_TICKS = 43 + 6;
    private static final int ORE_SENSE_TICKS = 10 + 6;
    private static final int DANGER_TICKS = 30 + 6;

    private boolean greeted = false;
    private int passiveTalkTimer = PASSIVE_TALK_INTERVAL_TICKS;

    // ======================= "Осторожно сзади" =======================
    private static final double WARN_RADIUS = 14.0;
    private static final int WARN_COOLDOWN_TICKS = 60;
    private boolean watchBehind = false;
    private int lastWarnTick = -1000;

    public HeyYaEntity(StandEntityType<HeyYaEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TALK_UNTIL, 0);
        entityData.define(SKIN, SKIN_DEFAULT);
        entityData.define(DANCING, false);
        entityData.define(DANCE_START, 0);
        entityData.define(DANCE_X, 0.0F);
        entityData.define(DANCE_Y, 0.0F);
        entityData.define(DANCE_Z, 0.0F);
    }

    // ======================= Скин =======================

    public int getSkin() {
        return entityData.get(SKIN);
    }

    /** Узнаёт скин, выбранный владельцем в меню JoJo (вкладка Stand Skins), и передаёт его клиентам. */
    private void syncSkin(LivingEntity user) {
        int wanted = IStandPower.getStandPowerOptional(user)
                .map(power -> power.getStandInstance()
                        .map(instance -> instance.getSelectedSkin()
                                .map(skinId -> FLOWEY_SKIN_ID.equals(skinId) ? SKIN_FLOWEY
                                        : CHARA_SKIN_ID.equals(skinId) ? SKIN_CHARA : SKIN_DEFAULT)
                                .orElse(SKIN_DEFAULT))
                        .orElse(SKIN_DEFAULT))
                .orElse(SKIN_DEFAULT);
        if (wanted != getSkin()) {
            entityData.set(SKIN, wanted);
        }
    }

    // ======================= Позиция на спине =======================

    /**
     * Где должен находиться стенд относительно владельца. Считается по плавно интерполированной позиции владельца,
     * поэтому рендерер использует тот же расчёт на каждом кадре - стенд "приклеен" к игроку без задержки.
     */
    public static Vector3d getHangPosition(LivingEntity user, float partialTick, int skin, float time) {
        double backDistance = skin == SKIN_CHARA ? CHARA_BACK_DISTANCE
                : skin == SKIN_FLOWEY ? FLOWEY_BACK_DISTANCE : BACK_DISTANCE;
        double hangHeight = skin == SKIN_CHARA ? CHARA_HOVER_HEIGHT
                : skin == SKIN_FLOWEY ? FLOWEY_HANG_HEIGHT : HANG_HEIGHT;
        double sideOffset = skin == SKIN_CHARA ? CHARA_SIDE_OFFSET
                : skin == SKIN_FLOWEY ? FLOWEY_SIDE_OFFSET : SIDE_OFFSET;
        double yawRad = Math.toRadians(MathHelper.rotLerp(partialTick, user.yBodyRotO, user.yBodyRot));
        double sin = Math.sin(yawRad);
        double cos = Math.cos(yawRad);
        double ux = MathHelper.lerp((double) partialTick, user.xo, user.getX());
        double uy = MathHelper.lerp((double) partialTick, user.yo, user.getY());
        double uz = MathHelper.lerp((double) partialTick, user.zo, user.getZ());
        // Игрок смотрит в (-sin, cos): спина - это (sin, -cos), правая сторона - (-cos, -sin)
        double x = ux + sin * backDistance - cos * sideOffset;
        double z = uz - cos * backDistance - sin * sideOffset;
        double y = uy + hangHeight - (user.isCrouching() ? 0.15 : 0.0);
        if (skin == SKIN_CHARA) {
            y += CHARA_BOB_AMPLITUDE * Math.sin(time * 0.12);      // парит: плавно покачивается
        }
        return new Vector3d(x, y, z);
    }

    private void hangOnUserBack(LivingEntity user) {
        Vector3d pos = getHangPosition(user, 1.0F, getSkin(), tickCount);
        setPos(pos.x, pos.y, pos.z);
        setDeltaMovement(Vector3d.ZERO);
        fallDistance = 0;

        yRot = user.yBodyRot;
        yRotO = user.yBodyRotO;
        yBodyRot = user.yBodyRot;
        yBodyRotO = user.yBodyRotO;
        yHeadRot = user.yBodyRot;
        yHeadRotO = user.yBodyRotO;
    }

    // ======================= Разговор =======================

    /** Запускает анимацию разговора на указанное число тиков (вызывать на сервере). */
    public void speak(int ticks) {
        if (!level.isClientSide()) {
            entityData.set(TALK_UNTIL, (int) level.getGameTime() + ticks);
        }
    }

    /** 0..1 - насколько сейчас "говорит" стенд (плавно затухает в конце). Работает и на клиенте. */
    public float getTalkIntensity(float partialTick) {
        float remaining = entityData.get(TALK_UNTIL) - ((float) level.getGameTime() + partialTick);
        if (remaining <= 0) {
            return 0;
        }
        return Math.min(1F, remaining / 8F);
    }

    // ======================= Фразы (зависят от скина) =======================
    private static final int DEFAULT_MOTIVATION_PHRASES = 8;
    private static final int DEFAULT_CALM_PHRASES = 3;

    public boolean isFlowey() {
        return getSkin() == SKIN_FLOWEY;
    }

    private TranslationTextComponent numbered(String keyBase, int count) {
        return new TranslationTextComponent(keyBase + (level.random.nextInt(count) + 1));
    }

    /** Префикс ключей фраз у скина ("flowey", "chara") или null у обычного стенда. */
    private String phraseSkin() {
        switch (getSkin()) {
        case SKIN_FLOWEY: return "flowey";
        case SKIN_CHARA: return "chara";
        default: return null;
        }
    }

    /** Сколько фраз каждого вида у скина (ключи chat.heyya.<скин>_<вид>.1 ... .N в lang-файлах). */
    private static int skinPhraseCount(String skin, String kind) {
        boolean chara = "chara".equals(skin);
        switch (kind) {
        case "summon": return 4;
        case "talk": return chara ? 14 : 7;
        case "motivation": return chara ? 5 : 3;
        case "find": return 2;
        case "calm": return chara ? 4 : 3;
        default: return 1;
        }
    }

    /** Случайная фраза вида kind: у скина своя, у обычного стенда - из defaultBase. */
    private TranslationTextComponent skinPhrase(String kind, String defaultBase, int defaultCount) {
        String skin = phraseSkin();
        return skin != null ? numbered("chat.heyya." + skin + "_" + kind + ".", skinPhraseCount(skin, kind))
                : numbered(defaultBase, defaultCount);
    }

    /** Фраза мотивации. */
    public TranslationTextComponent motivationMessage() {
        return skinPhrase("motivation", "chat.heyya.hey_ya_motivation.", DEFAULT_MOTIVATION_PHRASES);
    }

    /** Фраза "Успокойся". */
    public TranslationTextComponent calmMessage() {
        return skinPhrase("calm", "chat.heyya.hey_ya_calm.", DEFAULT_CALM_PHRASES);
    }

    /** Фраза при находке руды ("Ты пропустил это!"). */
    public TranslationTextComponent oreFoundMessage() {
        return phraseSkin() != null ? skinPhrase("find", "", 1)
                : new TranslationTextComponent("chat.heyya.hey_ya_ore_sense.found");
    }

    /** Фраза при включении/выключении охраны спины. */
    public TranslationTextComponent watchBehindMessage(boolean enabled) {
        String skin = phraseSkin();
        String key = (skin != null ? "chat.heyya." + skin + "_behind." : "chat.heyya.hey_ya_behind.") + (enabled ? "on" : "off");
        return new TranslationTextComponent(key);
    }

    /** Голос: talk_1 или talk_2 (случайно) + анимация челюсти. */
    public void sayTalk() {
        if (getSkin() == SKIN_CHARA) {
            int i = level.random.nextInt(3);
            int ticks = i == 0 ? CHARA_TALK_1_TICKS : (i == 1 ? CHARA_TALK_2_TICKS : CHARA_TALK_3_TICKS);
            speak(ticks, InitSounds.charaTalk(i));
            return;
        }
        if (getSkin() == SKIN_FLOWEY) {
            int i = level.random.nextInt(3);
            int ticks = i == 0 ? FLOWEY_TALK_1_TICKS : (i == 1 ? FLOWEY_TALK_2_TICKS : FLOWEY_TALK_3_TICKS);
            speak(ticks, InitSounds.floweyTalk(i));
            return;
        }
        boolean first = level.random.nextBoolean();
        speak(first ? TALK_1_TICKS : TALK_2_TICKS, first ? InitSounds.HEY_YA_TALK_1.get() : InitSounds.HEY_YA_TALK_2.get());
    }

    /** Случайная фраза из 50 в чат владельцу + голос. Используется способностью "Поговорить" и пассивными репликами. */
    public void sayRandomPhrase() {
        LivingEntity user = getUser();
        if (user instanceof PlayerEntity) {
            ((PlayerEntity) user).displayClientMessage(
                    skinPhrase("talk", "chat.heyya.hey_ya_talk.", TALK_PHRASES_COUNT)
                    .withStyle(TextFormatting.YELLOW), false);
        }
        sayTalk();
    }

    /** Приветствие при призыве: одна из 3 случайных фраз + голос. */
    private void sayGreeting(LivingEntity user) {
        if (user instanceof PlayerEntity) {
            ((PlayerEntity) user).displayClientMessage(
                    skinPhrase("summon", "chat.heyya.hey_ya_summon.", SUMMON_PHRASES_COUNT)
                    .withStyle(TextFormatting.GOLD), false);
        }
        sayTalk();
    }

    /**
     * Реакция на достижение игрока: фраза в чат (у каждого скина своя) + голос.
     * @param key ключ реакции: iron_tools, hot_stuff, diamonds, cover_me, the_end, nether, monster_hunter
     *            (chat.heyya.hey_ya_adv.<key>, chat.heyya.flowey_adv.<key>, chat.heyya.chara_adv.<key>)
     */
    public void sayAdvancement(String key) {
        if (level.isClientSide()) {
            return;
        }
        LivingEntity user = getUser();
        if (user instanceof PlayerEntity) {
            String skin = phraseSkin();
            String base = skin != null ? "chat.heyya." + skin + "_adv." : "chat.heyya.hey_ya_adv.";
            ((PlayerEntity) user).displayClientMessage(
                    new TranslationTextComponent(base + key).withStyle(TextFormatting.YELLOW), false);
        }
        if (!isDancing()) {         // во время танца стенд молчит
            sayTalk();
        }
    }

    /** Реплика при находке руд. */
    public void sayOreFound() {
        if (getSkin() == SKIN_CHARA) {
            speak(CHARA_ORE_TICKS, InitSounds.HEY_YA_CHARA_ORE.get());
            return;
        }
        if (getSkin() == SKIN_FLOWEY) {
            speak(FLOWEY_ORE_TICKS, InitSounds.HEY_YA_FLOWEY_ORE.get());
            return;
        }
        speak(ORE_SENSE_TICKS, InitSounds.HEY_YA_ORE_SENSE.get());
    }

    /** Предупреждение об опасности. */
    public void sayDanger() {
        if (getSkin() == SKIN_CHARA) {
            speak(CHARA_DANGER_TICKS, InitSounds.HEY_YA_CHARA_DANGER.get());
            return;
        }
        if (getSkin() == SKIN_FLOWEY) {
            speak(FLOWEY_DANGER_TICKS, InitSounds.HEY_YA_FLOWEY_DANGER.get());
            return;
        }
        speak(DANGER_TICKS, InitSounds.HEY_YA_DANGER.get());
    }

    /** Проигрывает звук голоса и запускает анимацию разговора (только на сервере). */
    private void speak(int animationTicks, SoundEvent voice) {
        if (level.isClientSide()) {
            return;
        }
        speak(animationTicks);
        // Звук привязан к самому стенду: пока он играет, источник движется вместе со стендом,
        // поэтому на бегу звук не становится тише и не пропадает
        level.playSound(null, this, voice, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    /** Приветствие при призыве и пассивная реплика раз в 3 минуты. */
    private void updateSpeechSchedule(LivingEntity user) {
        if (isDancing() || user.isSleeping()) {
            return;         // во время танца и сна владельца стенд молчит (без голоса и анимации головы)
        }
        if (!greeted) {
            if (tickCount >= 10) {          // небольшая пауза, чтобы владелец успел появиться
                greeted = true;
                passiveTalkTimer = PASSIVE_TALK_INTERVAL_TICKS;
                sayGreeting(user);
            }
            return;
        }
        if (--passiveTalkTimer <= 0) {
            if (getTalkIntensity(0F) <= 0) {   // не перебиваем, если стенд уже говорит
                passiveTalkTimer = PASSIVE_TALK_INTERVAL_TICKS;
                sayRandomPhrase();
            }
            else {
                passiveTalkTimer = 20;
            }
        }
    }

    // ======================= "Брось это!" =======================
    /** Сколько фраз при появлении находки (chat.heyya.hey_ya_throw.1 ... .2). */
    public static final int THROW_PHRASES_COUNT = 2;
    private static final double PICKUP_MIN_DISTANCE = 4.0;
    private static final double PICKUP_MAX_DISTANCE = 5.0;
    private UUID lastPickupId;
    private UUID lastAutoPickupId;

    // ======================= Автонаходка =======================
    /** Раз в сколько тиков стенд сам находит ресурсы: случайно от 10 до 15 минут. */
    public static final int AUTO_FIND_MIN_TICKS = 10 * 60 * 20;
    public static final int AUTO_FIND_MAX_TICKS = 15 * 60 * 20;
    private int autoFindTimer = -1;

    private int nextAutoFindDelay() {
        return AUTO_FIND_MIN_TICKS + level.random.nextInt(AUTO_FIND_MAX_TICKS - AUTO_FIND_MIN_TICKS + 1);
    }

    /** Раз в 10-15 минут стенд сам кидает находку с ресурсами (железо 50% / золото 30% / алмаз 20%). */
    private void updateAutoFind() {
        if (!greeted) {
            return;
        }
        if (autoFindTimer < 0) {
            autoFindTimer = nextAutoFindDelay();
        }
        if (--autoFindTimer <= 0) {
            autoFindTimer = nextAutoFindDelay();
            dropPickupNearUser(true);
        }
    }

    /** Способность «Используй это!»: находка с обычной наградой (PickupLoot.roll). */
    public void dropPickupNearUser() {
        dropPickupNearUser(false);
    }

    /**
     * Создаёт светящийся квадрат-находку в 4-5 блоках от владельца и говорит об этом.
     * @param resources true - автонаходка (ресурсы вместо оружия), false - обычная от способности
     */
    public void dropPickupNearUser(boolean resources) {
        if (level.isClientSide()) {
            return;
        }
        LivingEntity user = getUser();
        if (user == null) {
            return;
        }

        // Прошлая находка того же вида пропадает, чтобы на карте не копились квадраты
        // (ручная и автоматическая отслеживаются отдельно и друг друга не удаляют)
        UUID previous = resources ? lastAutoPickupId : lastPickupId;
        if (previous != null && level instanceof ServerWorld) {
            Entity old = ((ServerWorld) level).getEntity(previous);
            if (old != null) {
                old.remove();
            }
        }

        Vector3d spot = findPickupSpot(user);
        HeyYaPickupEntity pickup = new HeyYaPickupEntity(InitEntities.PICKUP.get(), level);
        pickup.setPos(spot.x, spot.y, spot.z);
        pickup.setResourceLoot(resources);
        level.addFreshEntity(pickup);
        if (resources) {
            lastAutoPickupId = pickup.getUUID();
        }
        else {
            lastPickupId = pickup.getUUID();
        }

        if (user instanceof PlayerEntity) {
            ((PlayerEntity) user).displayClientMessage(
                    skinPhrase("find", "chat.heyya.hey_ya_throw.", THROW_PHRASES_COUNT)
                    .withStyle(TextFormatting.YELLOW), false);
        }
        sayTalk();
    }

    /** Ищет место на земле в 4-5 блоках от владельца: свободный блок над твёрдым. */
    private Vector3d findPickupSpot(LivingEntity user) {
        int baseY = MathHelper.floor(user.getY());
        for (int attempt = 0; attempt < 20; attempt++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0;
            double dist = PICKUP_MIN_DISTANCE + level.random.nextDouble() * (PICKUP_MAX_DISTANCE - PICKUP_MIN_DISTANCE);
            int x = MathHelper.floor(user.getX() + Math.cos(angle) * dist);
            int z = MathHelper.floor(user.getZ() + Math.sin(angle) * dist);
            for (int dy = 2; dy >= -3; dy--) {
                BlockPos pos = new BlockPos(x, baseY + dy, z);
                BlockPos below = pos.below();
                if (level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                        && level.getFluidState(pos).isEmpty()
                        && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
                    return new Vector3d(x + 0.5, pos.getY(), z + 0.5);
                }
            }
        }
        // Запасной вариант: прямо перед игроком
        Vector3d look = user.getLookAngle();
        return new Vector3d(user.getX() + look.x * 2.0, user.getY(), user.getZ() + look.z * 2.0);
    }

    // ======================= Танец =======================
    /** Как далеко от игрока (в блоках) можно выбрать блок для танца. */
    public static final double DANCE_REACH = 16.0;
    /**
     * Радиус исчезновения во время танца: если владелец ушёл от танцующего стенда дальше этого расстояния (в блоках),
     * стенд исчезает (убирается). Обычный радиус стенда - 7 блоков (дальность в InitStands), во время танца он больше.
     */
    public static final double DANCE_MAX_USER_DISTANCE = 25.0;
    /**
     * Длина одного круга танца в тиках = длина музыки (dance_music.ogg, 12.7 с). Музыка и танец повторяются по кругу:
     * в начале каждого круга снова включается музыка, а анимация (10.75 с) растягивается на весь круг, поэтому
     * сильная доля музыки совпадает с началом кувырков.
     */
    public static final int DANCE_CYCLE_TICKS = 254;
    /**
     * У Flowey круг = длина её музыки (flowey_dance_music.ogg, 23.3 с = 48 долей, около 123 ударов в минуту).
     * За круг анимация Flowey (1 с = 2 доли) проигрывается ровно FLOWEY_DANCE_REPEATS раз, поэтому танец идёт в такт.
     */
    public static final int FLOWEY_DANCE_CYCLE_TICKS = 466;
    public static final int FLOWEY_DANCE_REPEATS = 24;
    /**
     * У Chara круг = длина её музыки (chara_dance_music.ogg, 22.06 с = 441 тик). В круг помещается вся анимация танца
     * (21.5 с) и короткий переход обратно в начальную позу, чтобы танец и музыка повторялись без рывка.
     */
    public static final int CHARA_DANCE_CYCLE_TICKS = 441;

    /** Длина круга танца в тиках для текущего скина (совпадает с длиной его музыки). */
    public int getDanceCycleTicks() {
        int skin = getSkin();
        return skin == SKIN_FLOWEY ? FLOWEY_DANCE_CYCLE_TICKS : skin == SKIN_CHARA ? CHARA_DANCE_CYCLE_TICKS : DANCE_CYCLE_TICKS;
    }
    /** Громкость музыки танца (больше 1 - слышно дальше: 2.0 примерно на 32 блока). */
    private static final float DANCE_MUSIC_VOLUME = 2.0F;
    private boolean danceMusicPlaying = false;
    private int lastMusicCycle = -1;

    /** Блок, на котором стоит стенд (только сервер): если его сломать, стенд вернётся на спину. */
    private BlockPos danceBlock;

    /** Стоит ли стенд на блоке (танцует или просто стоит, если скин не обычный). Работает и на клиенте. */
    public boolean isDancing() {
        return entityData.get(DANCING);
    }

    /** Сколько секунд прошло с начала танца (для Flowey: анимация 1 с повторяется по кругу). Работает и на клиенте. */
    public float getDanceTime(float partialTick) {
        return Math.max(0.0F, ((float) level.getGameTime() + partialTick - entityData.get(DANCE_START)) / 20.0F);
    }

    /** Сколько секунд прошло с начала текущего круга танца (0 ... 12.7). Работает и на клиенте. */
    public float getDanceCycleTime(float partialTick) {
        float elapsed = (float) level.getGameTime() + partialTick - entityData.get(DANCE_START);
        if (elapsed < 0) {
            return 0;
        }
        return (elapsed % getDanceCycleTicks()) / 20.0F;
    }

    private Vector3d getDanceSpot() {
        return new Vector3d(entityData.get(DANCE_X), entityData.get(DANCE_Y), entityData.get(DANCE_Z));
    }

    /**
     * Находит блок, на который смотрит владелец, и ставит стенд на его верхнюю грань.
     * Единственное условие: над блоком не должно быть другого блока.
     * @return null, если всё удалось, иначе сообщение для игрока о причине отказа
     */
    public TranslationTextComponent startDanceAtLookedBlock() {
        if (level.isClientSide()) {
            return null;
        }
        LivingEntity user = getUser();
        if (user == null) {
            return null;
        }
        Vector3d eye = user.getEyePosition(1.0F);
        Vector3d end = eye.add(user.getLookAngle().scale(DANCE_REACH));
        BlockRayTraceResult hit = level.clip(new RayTraceContext(eye, end,
                RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, user));
        if (hit.getType() != RayTraceResult.Type.BLOCK) {
            return new TranslationTextComponent("chat.heyya.hey_ya_dance.no_block");
        }
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getCollisionShape(level, pos);
        if (shape.isEmpty()) {
            return new TranslationTextComponent("chat.heyya.hey_ya_dance.no_block");
        }
        BlockPos above = pos.above();
        if (!level.getBlockState(above).getCollisionShape(level, above).isEmpty()) {
            return new TranslationTextComponent("chat.heyya.hey_ya_dance.blocked");
        }

        // Стенд стоит ровно на верхней грани: у обычного блока это y + 1, у плиты, снега и т.п. - их настоящая высота
        double top = pos.getY() + shape.max(Direction.Axis.Y);
        danceBlock = pos.immutable();
        entityData.set(DANCE_X, (float) (pos.getX() + 0.5));
        entityData.set(DANCE_Y, (float) top);
        entityData.set(DANCE_Z, (float) (pos.getZ() + 0.5));
        entityData.set(DANCE_START, (int) level.getGameTime());
        entityData.set(DANCING, true);
        setPos(pos.getX() + 0.5, top, pos.getZ() + 0.5);
        // Голос и анимация головы при начале танца не нужны: только музыка (у обычного скина)
        stopDanceMusic();
        lastMusicCycle = -1;
        updateDanceMusic();
        return null;
    }

    /** Отмена: стенд возвращается на спину владельца. */
    public void stopDance() {
        if (level.isClientSide()) {
            return;
        }
        danceBlock = null;
        entityData.set(DANCING, false);
        stopDanceMusic();
    }

    /** В начале каждого круга танца запускает музыку заново (так она повторяется по кругу). Только сервер. */
    private void updateDanceMusic() {
        int skin = getSkin();
        int cycle = (int) ((level.getGameTime() - entityData.get(DANCE_START)) / getDanceCycleTicks());
        if (cycle != lastMusicCycle) {
            lastMusicCycle = cycle;
            danceMusicPlaying = true;
            level.playSound(null, getX(), getY(), getZ(),
                    skin == SKIN_FLOWEY ? InitSounds.FLOWEY_DANCE_MUSIC.get()
                            : skin == SKIN_CHARA ? InitSounds.CHARA_DANCE_MUSIC.get() : InitSounds.HEY_YA_DANCE_MUSIC.get(),
                    SoundCategory.RECORDS, DANCE_MUSIC_VOLUME, 1.0F);
        }
    }

    /** Останавливает музыку у всех игроков рядом (отмена танца, стенд убран). Только сервер. */
    private void stopDanceMusic() {
        if (!danceMusicPlaying) {
            return;
        }
        danceMusicPlaying = false;
        if (level instanceof ServerWorld) {
            SStopSoundPacket[] packets = {
                    new SStopSoundPacket(InitSounds.HEY_YA_DANCE_MUSIC.get().getLocation(), SoundCategory.RECORDS),
                    new SStopSoundPacket(InitSounds.FLOWEY_DANCE_MUSIC.get().getLocation(), SoundCategory.RECORDS),
                    new SStopSoundPacket(InitSounds.CHARA_DANCE_MUSIC.get().getLocation(), SoundCategory.RECORDS)
            };
            for (ServerPlayerEntity player : ((ServerWorld) level).players()) {
                if (player.distanceToSqr(this) < 64.0 * 64.0) {
                    for (SStopSoundPacket packet : packets) {
                        player.connection.send(packet);
                    }
                }
            }
        }
    }

    @Override
    public void remove() {
        if (!level.isClientSide()) {
            stopDanceMusic();
        }
        super.remove();
    }

    /** Ставит стенд на точку танца и поворачивает лицом к владельцу. Вызывается на сервере и на клиенте. */
    private void standAtDanceSpot(LivingEntity user) {
        Vector3d spot = getDanceSpot();
        setPos(spot.x, spot.y, spot.z);
        setDeltaMovement(Vector3d.ZERO);
        fallDistance = 0;

        double dx = user.getX() - spot.x;
        double dz = user.getZ() - spot.z;
        if (dx * dx + dz * dz > 1.0E-4) {
            float yaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
            yRot = yaw;
            yRotO = yaw;
            yBodyRot = yaw;
            yBodyRotO = yaw;
            yHeadRot = yaw;
            yHeadRotO = yaw;
        }
    }

    /** Серверные проверки: владелец слишком далеко (стенд исчезает), блок сломали; музыка по кругу. */
    private void updateDance(LivingEntity user) {
        if (removed) {
            return;         // стенд уже убран (например, самим RotP)
        }
        Vector3d spot = getDanceSpot();
        if (user.distanceToSqr(spot.x, spot.y, spot.z) > DANCE_MAX_USER_DISTANCE * DANCE_MAX_USER_DISTANCE) {
            // Владелец ушёл слишком далеко: стенд исчезает (убирается, как при выходе за обычную дальность)
            stopDance();
            IStandPower.getStandPowerOptional(user).ifPresent(power -> {
                if (power.isActive()) {
                    power.toggleSummon();
                }
            });
            return;
        }
        if (tickCount % 10 == 0 && danceBlock != null
                && level.getBlockState(danceBlock).getCollisionShape(level, danceBlock).isEmpty()) {
            stopDance();
            return;
        }
        updateDanceMusic();
    }

    // ======================= Сон =======================
    /** На сколько блоков от позиции спящего игрока (в сторону ног) находится живот. Позиция спящего - у его головы. */
    private static final double SLEEP_BELLY_BACK = 0.55;
    /** На сколько пикселей модели (1 пиксель = 1/16 блока, с учётом размера стенда) опустить лежащего стенда: чтобы лежал на животе, а не в воздухе. */
    private static final double SLEEP_LOWER_PIXELS = 3.5;
    /** Высота стенда над позицией спящего игрока: живот лежащего чуть выше уровня кровати (0.28), минус SLEEP_LOWER_PIXELS. */
    private static final double SLEEP_BELLY_HEIGHT = 0.28 - SLEEP_LOWER_PIXELS * MODEL_SCALE / 16.0;
    /** Поправка к повороту стенда во сне в градусах: поставьте 180, если голова должна смотреть в другую сторону. */
    private static final float SLEEP_YAW_OFFSET = 0.0F;

    /**
     * Лежит ли стенд на животе спящего владельца. Работает и на клиенте.
     * У Flowey так не бывает: он остаётся на спине владельца и никак не анимируется.
     */
    public boolean isUserSleeping() {
        LivingEntity user = getUser();
        return user != null && user.isSleeping() && getSkin() != SKIN_FLOWEY;
    }

    /**
     * Стенд лежит на животе спящего владельца и повёрнут в его левую сторону. Обычный скин лежит на спине
     * (спиной к игроку) головой влево, остальные скины стоят лицом влево. Вызывается на сервере и на клиенте.
     */
    private void lieOnUserBelly(LivingEntity user) {
        // Направление кровати "от ног к голове" (то же самое, по чему рисуется лежащий игрок)
        Direction bed = user.getBedOrientation();
        double dirX;
        double dirZ;
        if (bed != null) {
            dirX = bed.getStepX();
            dirZ = bed.getStepZ();
        }
        else {
            float yaw = user.yBodyRot * ((float) Math.PI / 180.0F);
            dirX = -MathHelper.sin(yaw);
            dirZ = MathHelper.cos(yaw);
        }
        setPos(user.getX() - dirX * SLEEP_BELLY_BACK, user.getY() + SLEEP_BELLY_HEIGHT, user.getZ() - dirZ * SLEEP_BELLY_BACK);
        setDeltaMovement(Vector3d.ZERO);
        fallDistance = 0;

        // Левая сторона лежащего на спине игрока (голова в сторону dir): (-dirZ, 0, dirX)
        double leftX = -dirZ;
        double leftZ = dirX;
        float yaw;
        if (getSkin() == SKIN_DEFAULT) {
            // Лёжа, стенд "смотрит" головой в сторону своей правой руки (при повороте 0 - на запад): голова к левой стороне игрока
            yaw = (float) (MathHelper.atan2(-leftZ, -leftX) * (180.0 / Math.PI));
        }
        else {
            // Стоящий скин смотрит лицом влево
            yaw = (float) (MathHelper.atan2(-leftX, leftZ) * (180.0 / Math.PI));
        }
        yaw += SLEEP_YAW_OFFSET;
        yRot = yaw;
        yRotO = yaw;
        yBodyRot = yaw;
        yBodyRotO = yaw;
        yHeadRot = yaw;
        yHeadRotO = yaw;
    }

    // ======================= Тик =======================

    @Override
    public void tick() {
        // Пока стенд танцует на блоке дальше обычной дальности, перед проверками самого RotP ставим его к владельцу,
        // чтобы тот не убрал стенд раньше времени; после них стенд встаёт обратно на точку танца (дальше DANCE_MAX_USER_DISTANCE
        // стенд не прячем - там он исчезает сам).
        LivingEntity owner = getUser();
        if (owner != null && isDancing() && owner.isAlive() && !owner.isSleeping()
                && owner.distanceToSqr(this) <= DANCE_MAX_USER_DISTANCE * DANCE_MAX_USER_DISTANCE) {
            setPos(owner.getX(), owner.getY(), owner.getZ());
        }
        super.tick();

        LivingEntity user = getUser();
        if (user != null && user.isAlive()) {
            boolean ownerSleeping = user.isSleeping();
            if (ownerSleeping && !level.isClientSide() && isDancing()) {
                stopDance();        // владелец лёг в кровать: танец прерывается
            }
            if (isUserSleeping()) {
                lieOnUserBelly(user);       // стенд переходит со спины на живот (кроме Flowey: он остаётся на спине)
            }
            else if (isDancing() && !ownerSleeping) {
                standAtDanceSpot(user);
            }
            else {
                hangOnUserBack(user);
            }
            if (!level.isClientSide()) {
                syncSkin(user);
                updateSpeechSchedule(user);
                updateAutoFind();
                if (isDancing() && !ownerSleeping) {
                    updateDance(user);
                }
            }
        }

        if (!level.isClientSide() && watchBehind && tickCount % 10 == 0) {
            scanForMobsBehindUser();
        }
    }

    // ======================= Осторожно сзади =======================

    /** Переключает режим "Осторожно сзади". Возвращает новое состояние. */
    public boolean toggleWatchBehind() {
        watchBehind = !watchBehind;
        return watchBehind;
    }

    private void scanForMobsBehindUser() {
        final LivingEntity user = getUser();
        if (user == null || !user.isAlive()) {
            return;
        }
        if (tickCount - lastWarnTick < WARN_COOLDOWN_TICKS) {
            return;
        }

        double lookX = user.getLookAngle().x;
        double lookZ = user.getLookAngle().z;
        double lookLen = Math.sqrt(lookX * lookX + lookZ * lookZ);
        if (lookLen < 1.0E-4) {
            return;
        }

        List<MobEntity> hostiles = level.getEntitiesOfClass(MobEntity.class,
                user.getBoundingBox().inflate(WARN_RADIUS),
                mob -> mob instanceof IMob && mob.isAlive() && mob.getTarget() == user);

        MobEntity closestBehind = null;
        double closestDistSq = Double.MAX_VALUE;
        for (MobEntity mob : hostiles) {
            double dx = mob.getX() - user.getX();
            double dz = mob.getZ() - user.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < 1.0E-4) {
                continue;
            }
            double cos = (lookX * dx + lookZ * dz) / (lookLen * dist);
            if (cos < -0.2 && dist * dist < closestDistSq) {
                closestDistSq = dist * dist;
                closestBehind = mob;
            }
        }

        if (closestBehind != null && user instanceof PlayerEntity) {
            lastWarnTick = tickCount;
            sayDanger();
            ((PlayerEntity) user).displayClientMessage(
                    new TranslationTextComponent(phraseSkin() != null
                            ? "chat.heyya." + phraseSkin() + "_behind.warning" : "chat.heyya.hey_ya_behind.warning",
                            closestBehind.getDisplayName())
                    .withStyle(TextFormatting.RED), false);
        }
    }
}
