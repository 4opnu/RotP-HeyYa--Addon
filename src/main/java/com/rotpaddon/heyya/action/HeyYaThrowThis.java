package com.rotpaddon.heyya.action;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.heyya.entity.HeyYaEntity;

import net.minecraft.world.World;

// Способность "Брось это!": в 4-5 блоках от игрока появляется светящийся квадрат,
// подойти и нажать ПКМ - подобрать случайный предмет (шансы в PickupLoot).
public class HeyYaThrowThis extends StandEntityAction {

    public HeyYaThrowThis(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide() && standEntity instanceof HeyYaEntity) {
            ((HeyYaEntity) standEntity).dropPickupNearUser();
        }
    }
}
