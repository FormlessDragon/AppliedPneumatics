package com.wintercogs.appliedpneumatics.common.blocks.me_pressure_chamber;

import appeng.client.render.effects.ParticleTypes;
import appeng.core.AEConfig;
import appeng.core.AppEngClient;
import appeng.core.definitions.AEBlocks;
import appeng.decorative.solid.QuartzGlassBlock;
import com.wintercogs.appliedpneumatics.common.blocks.entitis.me_pressure_chamber.MEPressureChamberVibrantGlassBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class MEPressureChamberVibrantGlass extends MEPressureChamberBase implements EntityBlock, IChamberPartLike
{
    public MEPressureChamberVibrantGlass(Properties props)
    {
        super(props);
    }

    @Override
    public void onChamberFormed(ServerLevel level, BlockPos selfPos, FormedStructure fs)
    {
        super.onChamberFormed(level, selfPos, fs);
    }

    @Override
    public void onChamberBroken(ServerLevel level, BlockPos selfPos, Set<BlockPos> oldShell)
    {
        super.onChamberBroken(level, selfPos, oldShell);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r)
    {
        if (!AEConfig.instance().isEnableEffects())
        {
            return;
        }

        if (AppEngClient.instance().shouldAddParticles(r))
        {
            final double d0 = (r.nextFloat() - 0.5F) * 0.96D;
            final double d1 = (r.nextFloat() - 0.5F) * 0.96D;
            final double d2 = (r.nextFloat() - 0.5F) * 0.96D;

            level.addParticle(ParticleTypes.VIBRANT, 0.5 + pos.getX() + d0, 0.5 + pos.getY() + d1,
                    0.5 + pos.getZ() + d2, 0,
                    0, 0);
        }
    }

    @Override
    public @NotNull BlockState getAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side, @Nullable BlockState queryState, @Nullable BlockPos queryPos)
    {
        return AEBlocks.QUARTZ_GLASS.block().defaultBlockState();
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side)
    {
        if (adjacentBlockState.getBlock() instanceof QuartzGlassBlock
                && adjacentBlockState.getRenderShape() == state.getRenderShape())
        {
            return true;
        }

        return super.skipRendering(state, adjacentBlockState, side);
    }

    protected VoxelShape getVisualShape(BlockState p_309057_, BlockGetter p_308936_, BlockPos p_308956_, CollisionContext p_309006_)
    {
        return Shapes.empty();
    }

    protected float getShadeBrightness(BlockState p_308911_, BlockGetter p_308952_, BlockPos p_308918_)
    {
        return 1.0F;
    }

    protected boolean propagatesSkylightDown(BlockState p_309084_, BlockGetter p_309133_, BlockPos p_309097_)
    {
        return true;
    }


    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        return new MEPressureChamberVibrantGlassBlockEntity(blockPos, blockState);
    }
}
