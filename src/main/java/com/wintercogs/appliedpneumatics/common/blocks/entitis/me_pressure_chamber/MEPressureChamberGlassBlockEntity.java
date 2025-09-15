package com.wintercogs.appliedpneumatics.common.blocks.entitis.me_pressure_chamber;

import appeng.api.AECapabilities;
import appeng.api.networking.GridFlags;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.init.APBlockEntities;
import com.wintercogs.appliedpneumatics.common.init.APBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = AppliedPneumatics.MODID, bus = EventBusSubscriber.Bus.MOD)
public class MEPressureChamberGlassBlockEntity extends MEPressureChamberBaseBlockEntity
{
    public MEPressureChamberGlassBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(APBlockEntities.ME_PRESSURE_CHAMBER_GLASS_BLOCK_ENTITY.get(), pos, blockState,
                APBlocks.ME_PRESSURE_CHAMBER_GLASS, "me_pressure_chamber_glass_node", GridFlags.DENSE_CAPACITY);
    }


    @SubscribeEvent
    public static void onRegisterCaps(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                APBlockEntities.ME_PRESSURE_CHAMBER_GLASS_BLOCK_ENTITY.get(),
                (be, unused) -> be
        );
    }

}
