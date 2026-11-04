package com.rotpaddon.heyya.action;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.heyya.entity.HeyYaEntity;

import net.minecraft.world.World;

/**
 * Вариация способности «Танец» при зажатом Shift: отменяет танец, стенд возвращается на спину владельца.
 */
public class HeyYaDanceCancel extends StandEntityAction {

    public HeyYaDanceCancel(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide() && standEntity instanceof HeyYaEntity) {
            ((HeyYaEntity) standEntity).stopDance();
        }
    }
}
