package com.rotpaddon.heyya.action;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.heyya.entity.HeyYaEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

// Способность 3: "Осторожно сзади!" - переключатель (вкл/выкл).
// Пока включено, стенд предупреждает, если позади игрока есть враждебный моб, идущий на него.
// Сама проверка находится в HeyYaEntity#tick.
public class HeyYaWatchBehind extends StandEntityAction {

    public HeyYaWatchBehind(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide() && standEntity instanceof HeyYaEntity) {
            boolean enabled = ((HeyYaEntity) standEntity).toggleWatchBehind();
            LivingEntity user = userPower.getUser();
            if (user instanceof PlayerEntity) {
                ((PlayerEntity) user).displayClientMessage(
                        ((HeyYaEntity) standEntity).watchBehindMessage(enabled)
                        .withStyle(enabled ? TextFormatting.GOLD : TextFormatting.GRAY), false);
            }
        }
    }
}
