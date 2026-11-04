package com.rotpaddon.heyya.action;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.heyya.entity.HeyYaEntity;
import com.rotpaddon.heyya.entity.OreGlowEntity;
import com.rotpaddon.heyya.init.InitEntities;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

// Способность 4: "Ты пропустил это!" - подсвечивает руды в радиусе 6 блоков от игрока.
// Сама руда начинает светиться: на её месте рисуется светящийся блок, контур которого виден сквозь стены.
// Через несколько секунд подсветка исчезает сама.
public class HeyYaOreSense extends StandEntityAction {
    private static final int RADIUS = 6;
    private static final int MAX_MARKERS = 60;
    // Сколько тиков горит подсветка (100 = 5 секунд)
    private static final int HIGHLIGHT_TICKS = 100;

    public HeyYaOreSense(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (world.isClientSide()) {
            return;
        }
        LivingEntity user = userPower.getUser();
        if (user == null) {
            return;
        }

        BlockPos center = user.blockPosition();
        int found = 0;
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    if (dx * dx + dy * dy + dz * dz > RADIUS * RADIUS) {
                        continue;
                    }
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = world.getBlockState(pos);
                    if (!isOre(state)) {
                        continue;
                    }
                    found++;
                    if (found <= MAX_MARKERS) {
                        spawnGlow(world, pos, state);
                    }
                }
            }
        }

        if (found > 0 && standEntity instanceof HeyYaEntity) {
            ((HeyYaEntity) standEntity).sayOreFound();
        }

        if (user instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) user;
            if (found > 0) {
                // Количество найденных руд в сообщении не указываем
                TranslationTextComponent message = standEntity instanceof HeyYaEntity
                        ? ((HeyYaEntity) standEntity).oreFoundMessage()
                        : new TranslationTextComponent("chat.heyya.hey_ya_ore_sense.found");
                player.displayClientMessage(message.withStyle(TextFormatting.AQUA), false);
            }
            else {
                player.displayClientMessage(
                        new TranslationTextComponent("chat.heyya.hey_ya_ore_sense.none")
                        .withStyle(TextFormatting.GRAY), false);
            }
        }
    }

    private static boolean isOre(BlockState state) {
        return state.is(Tags.Blocks.ORES) || state.is(Blocks.ANCIENT_DEBRIS);
    }

    private static void spawnGlow(World world, BlockPos pos, BlockState state) {
        OreGlowEntity glow = new OreGlowEntity(InitEntities.ORE_GLOW.get(), world);
        glow.setup(state, HIGHLIGHT_TICKS);
        glow.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        world.addFreshEntity(glow);
    }
}
