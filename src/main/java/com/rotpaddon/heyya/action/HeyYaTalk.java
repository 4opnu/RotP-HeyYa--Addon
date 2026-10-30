package com.rotpaddon.heyya.action;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.heyya.entity.HeyYaEntity;

import net.minecraft.world.World;

// Способность 1: Hey Ya! говорит одну из 50 фраз на тему Майнкрафта (сама логика - в HeyYaEntity#sayRandomPhrase).
public class HeyYaTalk extends StandEntityAction {

    public HeyYaTalk(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide() && standEntity instanceof HeyYaEntity) {
            ((HeyYaEntity) standEntity).sayRandomPhrase();
        }
    }
}
