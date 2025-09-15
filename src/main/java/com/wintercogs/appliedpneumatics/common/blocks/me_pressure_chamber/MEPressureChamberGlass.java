package com.wintercogs.appliedpneumatics.common.blocks.me_pressure_chamber;

import com.wintercogs.appliedpneumatics.common.blocks.entitis.me_pressure_chamber.MEPressureChamberGlassBlockEntity;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class MEPressureChamberGlass extends MEPressureChamberBase implements EntityBlock, IChamberPartLike
{
    public MEPressureChamberGlass(Properties props)
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

    public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter world, BlockPos pos, FluidState fluidState)
    {
        return true;
    }

    public boolean skipRendering(BlockState ourState, BlockState theirState, Direction side)
    {
        return ourState.getBlock() == theirState.getBlock() || super.skipRendering(ourState, theirState, side);
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos)
    {
        return 0.2f;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos)
    {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> curInfo, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, curInfo, flag);
        if (!ModList.get().isLoaded("ctm") && !ModList.get().isLoaded("fusion"))
        {
            curInfo.add(PneumaticCraftUtils.xlate("gui.tooltip.block.pneumaticcraft.pressure_chamber_glass.ctm", new Object[0]).withStyle(ChatFormatting.GRAY));
        }
    }


    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        return new MEPressureChamberGlassBlockEntity(blockPos, blockState);
    }
}
