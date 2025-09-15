package com.wintercogs.appliedpneumatics.common.blocks.entitis.me_pressure_chamber;

import appeng.api.AECapabilities;
import appeng.api.networking.GridFlags;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.blocks.me_pressure_chamber.FormedStructure;
import com.wintercogs.appliedpneumatics.common.blocks.me_pressure_chamber.MEPressureChamberBase;
import com.wintercogs.appliedpneumatics.common.init.APBlockEntities;
import com.wintercogs.appliedpneumatics.common.init.APBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = AppliedPneumatics.MODID, bus = EventBusSubscriber.Bus.MOD)
public class MEPressureChamberValveBlockEntity extends MEPressureChamberBaseBlockEntity
{


    public MEPressureChamberValveBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(APBlockEntities.ME_PRESSURE_CHAMBER_VALVE_BLOCK_ENTITY.get(), pos, blockState,
                APBlocks.ME_PRESSURE_CHAMBER_VALVE, "me_pressure_chamber_valve_node", GridFlags.DENSE_CAPACITY);
    }

    public void onChamberFormed()
    {
        setChanged();
    }




    @SubscribeEvent
    public static void onRegisterCaps(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                APBlockEntities.ME_PRESSURE_CHAMBER_VALVE_BLOCK_ENTITY.get(),
                (be, unused) -> be
        );
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        if(tag.contains("formedStructure"))
        {
            this.formedStructure = FormedStructure.fromNBT(tag.getCompound("formedStructure"));

        }
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        if(level instanceof ServerLevel serverLevel && formedStructure != null && formedStructure.controllerPos().equals(worldPosition))
            MEPressureChamberBase.rebroadcastFormedAfterLoad(serverLevel, formedStructure, true, false);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        if(level instanceof ServerLevel && formedStructure != null && formedStructure.controllerPos().equals(worldPosition))
        {
            tag.put("formedStructure", formedStructure.toNBT());
        }
    }
}
