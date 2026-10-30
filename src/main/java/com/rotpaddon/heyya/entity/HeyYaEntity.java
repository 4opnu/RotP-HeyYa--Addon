package com.rotpaddon.heyya.entity;

import java.util.List;
import java.util.UUID;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.heyya.AddonMain;
import com.rotpaddon.heyya.init.InitEntities;
import com.rotpaddon.heyya.init.InitSounds;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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

    /** Создаёт светящийся квадрат-находку в 4-5 блоках от владельца и говорит об этом. */
    public void dropPickupNearUser() {
        if (level.isClientSide()) {
            return;
        }
        LivingEntity user = getUser();
        if (user == null) {
            return;
        }

        // Прошлая находка пропадает, чтобы на карте не копились квадраты
        if (lastPickupId != null && level instanceof ServerWorld) {
            Entity old = ((ServerWorld) level).getEntity(lastPickupId);
            if (old != null) {
                old.remove();
            }
        }

        Vector3d spot = findPickupSpot(user);
        HeyYaPickupEntity pickup = new HeyYaPickupEntity(InitEntities.PICKUP.get(), level);
        pickup.setPos(spot.x, spot.y, spot.z);
        level.addFreshEntity(pickup);
        lastPickupId = pickup.getUUID();

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

    // ======================= Тик =======================

    @Override
    public void tick() {
        super.tick();

        LivingEntity user = getUser();
        if (user != null && user.isAlive()) {
            hangOnUserBack(user);
            if (!level.isClientSide()) {
                syncSkin(user);
                updateSpeechSchedule(user);
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
