package com.wintercogs.appliedpneumatics.common.blocks;

import com.wintercogs.appliedpneumatics.common.blocks.entitis.MEPressureInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
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
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult)
    {
        super.useWithoutItem(state,level,pos,player,hitResult);
        if(!level.isClientSide()&&!player.isShiftKeyDown())
        {
            player.openMenu((MEPressureInterfaceBlockEntity)level.getBlockEntity(pos),pos);
        }
        return InteractionResult.SUCCESS_NO_ITEM_USED;
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

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston)
    {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof MEPressureInterfaceBlockEntity blockEntity) {
                level.updateNeighbourForOutputSignal(pos, this);
                blockEntity.dropContent();
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }
}
