package com.wintercogs.appliedpneumatics.common.blocks;

import appeng.block.AEBaseEntityBlock;
import com.wintercogs.appliedpneumatics.common.blocks.entitis.MEAmadronProcessStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class MEAmadronProcessStation extends AEBaseEntityBlock<MEAmadronProcessStationBlockEntity>
{
    public MEAmadronProcessStation(Properties properties)
    {
        super(properties);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult)
    {
        super.useWithoutItem(state,level,pos,player,hitResult);
        if(!level.isClientSide()&&!player.isShiftKeyDown())
        {
            player.openMenu((MEAmadronProcessStationBlockEntity)level.getBlockEntity(pos),pos);
        }
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }

}
