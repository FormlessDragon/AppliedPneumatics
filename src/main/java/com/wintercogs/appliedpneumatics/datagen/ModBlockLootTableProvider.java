package com.wintercogs.appliedpneumatics.datagen;

import com.wintercogs.appliedpneumatics.common.init.APBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider
{

    protected ModBlockLootTableProvider(HolderLookup.Provider registries)
    {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate()
    {
        dropSelf(APBlocks.ME_PRESSURE_INTERFACE_BLOCK.get());
        dropSelf(APBlocks.ME_PRESSURE_CHAMBER_VALVE.get());
        dropSelf(APBlocks.ME_PRESSURE_CHAMBER_WALL.get());
        dropSelf(APBlocks.ME_PRESSURE_CHAMBER_GLASS.get());
        dropSelf(APBlocks.ME_PRESSURE_CHAMBER_VIBRANT_GLASS.get());
        dropSelf(APBlocks.ME_AMADRON_PROCESS_STATION.get());
        dropSelf(APBlocks.ME_TEMPERATURE_INTERFACE.get());
        dropSelf(APBlocks.ME_AMADRON_EXTENDED_PROCESS_STATION.get());
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks()
    {
        return APBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
