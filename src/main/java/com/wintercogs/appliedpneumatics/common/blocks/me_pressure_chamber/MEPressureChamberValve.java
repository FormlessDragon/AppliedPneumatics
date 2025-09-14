package com.wintercogs.appliedpneumatics.common.blocks.me_pressure_chamber;

import com.wintercogs.appliedpneumatics.common.blocks.entitis.MEPressureChamberValveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class MEPressureChamberValve extends MEPressureChamberBase implements EntityBlock , MEPressureChamberBase.IChamberControllerLike
{
    public MEPressureChamberValve(Properties properties)
    {
        super(properties);
    }

    @Override
    public void onControlFormed(ServerLevel level, BlockPos controllerPos, FormedStructure fs)
    {

    }

    @Override
    public void onControlBroken(ServerLevel level, BlockPos controllerPos, Set<BlockPos> oldShell)
    {

    }

    @Override
    public void onChamberFormed(ServerLevel level, BlockPos selfPos, FormedStructure fs)
    {

    }

    @Override
    public void onChamberBroken(ServerLevel level, BlockPos selfPos, Set<BlockPos> oldShell)
    {

    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState)
    {
        return new MEPressureChamberValveBlockEntity(blockPos, blockState);
    }
}
