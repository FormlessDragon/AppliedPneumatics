package com.wintercogs.appliedpneumatics.common.blocks.entitis;

import com.wintercogs.appliedpneumatics.common.init.APBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MEPressureChamberValveBlockEntity extends BlockEntity
{
    public MEPressureChamberValveBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(APBlockEntities.ME_PRESSURE_CHAMBER_VALVE_BLOCK_ENTITY.get(), pos, blockState);
    }
}
