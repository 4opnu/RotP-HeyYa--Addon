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

/**
 * Способность «Танец»: стенд перемещается со спины на блок, на который смотрит игрок, и встаёт на его верхнюю грань.
 * Обычный скин танцует (анимация из Dance.bbmodel, с музыкой), Flowey - своим танцем (Flowery_dance.bbmodel, с её музыкой),
 * у Chara - своим танцем (Dance_chara_4.bbmodel, с её музыкой).
 * Условие одно: над выбранным блоком не должно быть другого блока.
 * Отменить можно вариацией с Shift (HeyYaDanceCancel). Вся логика находится в HeyYaEntity#startDanceAtLookedBlock.
 */
public class HeyYaDance extends StandEntityAction {

    public HeyYaDance(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (world.isClientSide() || !(standEntity instanceof HeyYaEntity)) {
            return;
        }
        TranslationTextComponent error = ((HeyYaEntity) standEntity).startDanceAtLookedBlock();
        LivingEntity user = userPower.getUser();
        if (error != null && user instanceof PlayerEntity) {
            ((PlayerEntity) user).displayClientMessage(error.withStyle(TextFormatting.GRAY), true);
        }
    }
}
