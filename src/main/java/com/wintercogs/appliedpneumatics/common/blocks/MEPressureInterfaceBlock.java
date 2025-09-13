package com.wintercogs.appliedpneumatics.common.blocks;

import com.wintercogs.appliedpneumatics.common.blocks.entitis.MEPressureInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ME气压接口
 */
public class MEPressureInterfaceBlock extends Block implements EntityBlock
{

    public MEPressureInterfaceBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState)
    {
        return new MEPressureInterfaceBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType)
    {
        if(level.isClientSide())
            return null;

        return (level1, blockPos, blockState, blockEntity) -> {
            if(blockEntity instanceof MEPressureInterfaceBlockEntity be)
            {
                MEPressureInterfaceBlockEntity.serverTick(level1, blockPos, blockState, be);
            }
        };
    }
}
